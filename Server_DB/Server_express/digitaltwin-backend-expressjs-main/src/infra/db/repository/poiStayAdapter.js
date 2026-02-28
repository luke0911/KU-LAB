const { osClient } = require('../openSearchClient');
const { getISOWeekAndYear, getWeeksInRange } = require('../../../util/dateUtils');
const PoiStay = require('../../../domain/model/poiStay');

exports.saveToOpenSearch = async (logData) => {
  try {
    // 주차별 인덱스
    const { year, week } = getISOWeekAndYear(logData.timestamp);
    const indexName = `poi_stay_logs_${year}-${week}`;

    // 1. 인덱스 존재 여부 확인
    const exists = await osClient.indices.exists({ index: indexName });
    
    if (!exists.body) {
      // 2. 인덱스 생성 (매핑 정의)
      await osClient.indices.create({
        index: indexName,
        body: {
          mappings: {
            properties: {
              mapId:        { type: 'integer' },
              userId:       { type: 'keyword' },
              poiId:        { type: 'integer' },
              enterTime:    { type: 'date' },
              exitTime:     { type: 'date' },
              stayDuration: { type: 'double' }, // 단위: 초
              timestamp:    { type: 'date' }
            }
          }
        }
      });
    }

    // 3. 데이터 인덱싱
    await osClient.index({
      index: indexName,
      body: logData,
    });
    console.log(`[OpenSearch] POI 체류 로그 저장 완료: ${indexName}`);
  } catch (err) {
    console.error('[OpenSearch] Error indexing poi_stay_log:', err);
    throw err;
  }
};

exports.getLatestPoiStayLogbyUserId = async (userId) => {
  try {
    const response = await osClient.search({
      index: 'poi_stay_logs_*',
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
      console.warn(`[OpenSearch] No poi stay logs found for user ${userId}`);
      return null;
    }

    console.log(`[OpenSearch] Latest poi stay log retrieved successfully for user ${userId}`);
    return hits[0]._source;
  } catch (err) {
    console.error('[OpenSearch] Error retrieving latest poi stay log:', err.message);
    throw err;
  }
};

exports.getAllPoiStayLogs = async (userId, page, limit) => {
  const from = (page - 1) * limit;

  try {
    const response = await osClient.search({
      index: 'poi_stay_logs_*',
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

    console.log(`[OpenSearch] poi stay logs retrieved for user ${userId}, page ${page}`);
    return response.body.hits.hits.map(hit => hit._source);
  } catch (err) {
    console.error('[OpenSearch] Error getting poi stay logs:', err);
    throw err;
  }
};

exports.getPoiStayLogsByPeriod = async (userId, fromISO, toISO, page, limit) => {
  const from = (page - 1) * limit;
  const indexNames = getWeeksInRange(fromISO, toISO, 'poi_stay_logs'); // ["user_locations-2025-W20", ...]
  
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
    console.warn(`[OpenSearch] No valid poi stay logs indices found in the range from ${fromISO} to ${toISO}`);
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
              { match: { userId: userId } }
            ],
            should: [
              {
                range: {
                  enterTime: {
                    gte: fromISO,
                    lte: toISO,
                    format: 'strict_date_optional_time'
                  }
                }
              },
              {
                range: {
                  exitTime: {
                    gte: fromISO,
                    lte: toISO,
                    format: 'strict_date_optional_time'
                  }
                }
              }
            ],
            minimum_should_match: 1
          }
        },
        sort: [
          { enterTime: { order: 'desc' } }
        ]
      }
    });

    console.log(`[OpenSearch] POI Stay Logs from ${fromISO} to ${toISO} retrieved successfully for user ${userId}, page ${page} from valid indices:`, validIndexNames);
    return response.body.hits.hits.map(hit => hit._source);
  } catch (err) {
    console.error('[OpenSearch] Error retrieving POI Stay Logs by period:', err.message);
    throw err;
  }
};

exports.fromPoiChangeAdapter = (logInfo) => {
  return PoiStay.fromPoiChange({
    ...logInfo,
    prevPoi: logInfo.prevArea,                   // 어댑터 변환
    prevPoiEnterTime: logInfo.prevEnterTime,     // 어댑터 변환
    curTime: logInfo.curTime
  });
}