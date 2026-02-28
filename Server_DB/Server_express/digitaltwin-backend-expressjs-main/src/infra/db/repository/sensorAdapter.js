const { withRedisClient } = require('../redisClient');
const { osClient } = require('../openSearchClient');
const { getISOWeekAndYear, getWeeksInRange } = require('../../../util/dateUtils');

// Redis에 센서 데이터 저장
exports.saveToRedis = async (sensorData) => {
  const key = `recent-sensor:${sensorData.userId}`;
  const value = JSON.stringify(sensorData.toJSON());

  await withRedisClient(async (client) => {
    await client.set(key, value, { EX: 3600 });
    console.log(`[Redis] Saved sensorData with key: ${key} (expires in 1 hour)`);
  });
};

// OpenSearch에 센서 데이터 저장
exports.saveToOpenSearch = async (sensorData) => {
  try {
    const { year, week } = getISOWeekAndYear(sensorData.timestamp);
    const indexName = `user_sensors_${year}-${week}`;

    // 1. 인덱스 존재 여부 확인
    const exists = await osClient.indices.exists({ index: indexName });

    if (!exists.body) {
      // 2. 매핑 포함하여 인덱스 생성
      await osClient.indices.create({
        index: indexName,
        body: {
          mappings: {
            properties: {
              mapId:        { type: 'integer' },
              userId:       { type: 'keyword' },
              userName:     { type: 'keyword' },
              acceleration: {
                type: 'object',
                properties: {
                  x: { type: 'double' },
                  y: { type: 'double' },
                  z: { type: 'double' }
                }
              },
              magnetic: {
                type: 'object',
                properties: {
                  x: { type: 'double' },
                  y: { type: 'double' },
                  z: { type: 'double' }
                }
              },
              gyro: {
                type: 'object',
                properties: {
                  x: { type: 'double' },
                  y: { type: 'double' },
                  z: { type: 'double' }
                }
              },
              linearAcceleration: {
                type: 'object',
                properties: {
                  x: { type: 'double' },
                  y: { type: 'double' },
                  z: { type: 'double' }
                }
              },
              pressure: { type: 'double' },
              rotation: {
                type: 'object',
                properties: {
                  x: { type: 'double' },
                  y: { type: 'double' },
                  z: { type: 'double' }
                }
              },
              light: { type: 'double' },
              proximity: { type: 'double' },
              rf: { type: 'double' },
              stepLength: { type: 'double' },
              userStateReal: { type: 'keyword' },
              timestamp: { type: 'date' }
            }
          }
        }
      });
    }

    // 3. 데이터 인덱싱
    await osClient.index({
      index: indexName,
      body: sensorData.toJSON(),
    });
    console.log(`[OpenSearch] Sensor data indexed successfully to ${indexName}`);
  } catch (err) {
    console.error('[OpenSearch] Error indexing sensor data:', err);
    throw err;
  }
};

// 특정 사용자의 최신 센서 로그 1건 조회
exports.getLatestSensorLogByUserId = async (userId) => {
  try {
    const response = await osClient.search({
      index: 'user_sensors_*',
      size: 1,
      body: {
        query: {
          match: { userId: userId }
        },
        sort: [
          { timestamp: { order: 'desc' } }
        ]
      }
    });

    const hits = response.body.hits.hits;

    if (hits.length === 0) {
      console.warn(`[OpenSearch] No sensor logs found for user ${userId}`);
      return null;
    }

    console.log(`[OpenSearch] Latest sensor log retrieved successfully for user ${userId}`);
    return hits[0]._source;
  } catch (err) {
    console.error('[OpenSearch] Error retrieving latest sensor log:', err.message);
    throw err;
  }
};

// 특정 사용자 최신 센서 로그 조회 - Redis
exports.getRecentSensorLog = async (userId) => {
  return await withRedisClient(async (client) => {
    const key = `recent-sensor:${userId}`;
    const rawData = await client.get(key);
    if (rawData) {
      return JSON.parse(rawData);
    } else {
      return null; // 센서 정보가 없을 때 null 반환
    }
  });
};

// 특정 사용자 전체 센서 로그 조회
exports.getAllUserSensorLogs = async (userId, page, limit) => {
  const from = (page - 1) * limit;

  try {
    const response = await osClient.search({
      index: 'user_sensors_*',
      from, // 시작 offset
      size: limit, // 페이지당 문서 수
      body: {
        query: {
          match: { userId: userId }
        },
        sort: [
          { timestamp: { order: 'desc' } }
        ]
      }
    });

    console.log('[OpenSearch] Sensor logs retrieved successfully');
    return response.body.hits.hits.map(hit => hit._source);
  } catch (err) {
    console.error('[OpenSearch] Error getting sensor logs:', err);
    throw err;
  }
};

// 특정 기간 동안의 센서 로그 조회 (userId + timestamp 범위 조건)
exports.getSensorLogsByPeriod = async (userId, fromISO, toISO, page, limit) => {
  const from = (page - 1) * limit;
  const indexNames = getWeeksInRange(fromISO, toISO, 'user_sensors'); // ["user_sensors-2025-W20", ...]
  
  // 1. 각 인덱스 존재 여부 확인
  const indexChecks = await Promise.all(indexNames.map(async (index) => {
    try {
      const exists = await osClient.indices.exists({ index });
      return { index, exists: exists.body };
    } catch (err) {
      console.error(`[OpenSearch] Error checking existence for index "${index}":`, err.message);
      return { index, exists: false };
    }
  }));

  const validIndexNames = indexChecks.filter(entry => entry.exists).map(entry => entry.index);
  const invalidIndexNames = indexChecks.filter(entry => !entry.exists).map(entry => entry.index);

  // 2. 없는 인덱스 로그 출력
  if (invalidIndexNames.length > 0) {
    console.warn(`[OpenSearch] The following indices do not exist and will be skipped:`, invalidIndexNames);
  }

  // 3. 유효한 인덱스가 없으면 빈 배열 반환
  if (validIndexNames.length === 0) {
    console.warn(`[OpenSearch] No valid indices found in the range from ${fromISO} to ${toISO}`);
    return [];
  }

  // 4. 존재하는 인덱스에 대해서만 검색
  try {
    const response = await osClient.search({
      index: validIndexNames,
      from, // 시작 offset
      size: limit, // 페이지당 문서 수
      body: {
        query: {
          bool: {
            must: [
              { match: { userId: userId } },
              {
                range: {
                  timestamp: {
                    gte: fromISO,
                    lte: toISO,
                    format: 'strict_date_optional_time'
                  }
                }
              }
            ]
          }
        },
        sort: [
          { timestamp: { order: 'desc' } }
        ]
      }
    });

    console.log(`[OpenSearch] Sensor logs from ${fromISO} to ${toISO} retrieved successfully for user ${userId}, page ${page} from valid indices:`, validIndexNames);
    return response.body.hits.hits.map(hit => hit._source);
  } catch (err) {
    console.error('[OpenSearch] Error retrieving sensor logs by period:', err.message);
    throw err;
  }
};
