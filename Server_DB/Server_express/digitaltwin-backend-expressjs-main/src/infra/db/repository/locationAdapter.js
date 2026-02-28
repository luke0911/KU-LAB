const { withRedisClient } = require('../redisClient');
const { osClient } = require('../openSearchClient');
const { getISOWeekAndYear, getWeeksInRange } = require('../../../util/dateUtils');
const booleanPointInPolygon = require('@turf/boolean-point-in-polygon').default;
const { host } = require('../../../config/env');
const axios = require('axios');

exports.saveToRedis = async (location) => {
  const key = `recent-location:${location.userId}`;
  const value = JSON.stringify(location.toJSON());

  await withRedisClient(async (client) => {
    await client.set(key, value, { EX: 3600 });
    console.log(`[Redis] Saved location with key: ${key} (expires in 1 hour)`);

    // 위치 업데이트 이벤트 발행
    await client.publish('location_updates', 'updated');
  });
};

exports.getLatestLocationbyUserId = async (userId) => {
  try {
    const response = await osClient.search({
      index: 'user_locations_*',
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
      console.warn(`[OpenSearch] No location logs found for user ${userId}`);
      return null;
    }

    console.log(`[OpenSearch] Latest location log retrieved successfully for user ${userId}`);
    return hits[0]._source;
  } catch (err) {
    console.error('[OpenSearch] Error retrieving latest location log:', err.message);
    throw err;
  }
};

exports.getRecentLocationByUserId = async (userId) => {
  return await withRedisClient(async (client) => {
    const key = `recent-location:${userId}`;
    const rawData = await client.get(key);
    if (rawData) {
      return JSON.parse(rawData);
    } else {
      return null; // 위치 정보가 없을 때 null 반환
    }
  });
};

exports.getAllRecentLocations = async () => {
  return await withRedisClient(async (client) => {
    const locations = [];
    let cursor = '0';

    do {
      const reply = await client.scan(cursor, {
        MATCH: 'recent-location:*',
        COUNT: 100,
      });

      cursor = reply.cursor;
      const keys = reply.keys;

      for (const key of keys) {
        const rawData = await client.get(key);
        if (rawData) {
          locations.push(JSON.parse(rawData));
        }
      }
    } while (cursor !== '0');

    console.log(`[Redis] All users' recent locations are retrieved successfully`);
    return locations;
  });
};

exports.saveToOpenSearch = async (location) => {
  try {
    const { year, week } = getISOWeekAndYear(location.timestamp);
    const indexName = `user_locations_${year}-${week}`;

    // 1. 인덱스 존재 여부 확인
    const exists = await osClient.indices.exists({ index: indexName });

    if (!exists.body) {
      // 2. 매핑 포함하여 인덱스 생성
      await osClient.indices.create({
        index: indexName,
        body: {
          mappings: {
            properties: {
              mapId:         { type: 'integer' },
              userId:        { type: 'keyword' },
              userName:      { type: 'keyword' },
              ageRange:      { type: 'keyword' },
              gender:        { type: 'keyword' },
              userX:         { type: 'double' },
              userY:         { type: 'double' },
              userZ:         { type: 'double' },
              userDirection: { type: 'double' },
              userFloor:     { type: 'float' },
              matchedPoi:    { type: 'integer' },
              poiEnterTime:  { type: 'date' },
              matchedEventArea:    { type: 'integer' },
              eventAreaEnterTime:  { type: 'date' },
              userStatus:    { type: 'keyword' },
              background:    { type: 'boolean' },
              timestamp:     { type: 'date' }
            }
          }
        }
      });
    }

    // 3. 데이터 인덱싱
    await osClient.index({
      index: indexName,
      body: location.toJSON(),
    });
    console.log(`[OpenSearch] Location indexed successfully to ${indexName}`);
  } catch (err) {
    console.error('[OpenSearch] Error indexing location:', err);
    throw err;
  }
};

// 공통 Area 목록 조회 (POI/EventArea 등)
async function getAreasForMapAndFloor(mapId, floor, authHeader, { areaType }) {
  const cacheKey = `${areaType}s::map:${mapId}:floor:${floor}`;
  return await withRedisClient(async (client) => {
    let areasJson = await client.get(cacheKey);
    if (areasJson) {
      return JSON.parse(areasJson);
    }
    // API endpoint 구분
    let url;
    if (areaType === 'poi') {
      url = `${host}/spring/api/pois?mapId=${mapId}&floor=${floor}`;
    } else if (areaType === 'eventArea') {
      url = `${host}/spring/api/event-areas?mapId=${mapId}&floor=${floor}`;
    } else {
      throw new Error(`Unknown areaType: ${areaType}`);
    }
    const response = await axios.get(url, { headers: { Authorization: authHeader } });
    // data 포맷에 따라 추출
    const areas = response.data.data || [];
    return areas;
  });
}

// (x, y)값이 polygon 내에 포함되는 Area(POI/EventArea 등) 찾기
/**
 * @param location: { userX, userY, mapId, userFloor, ... }
 * @param areaType: 'poi', 'eventArea' 등
 * @param getAreaPoints: area 객체 → polygon points 추출 (예: poi => poi.poiPoints)
 * @param getAreaFloor: area 객체 → floor 추출 (예: poi => poi.poiFloor)
 */
exports.findAreaIncludingLocation = async (location, authHeader, areaType, getAreaPoints, getAreaFloor) => {
  const mapId = location.mapId;
  const floor = location.userFloor;
  const areas = await getAreasForMapAndFloor(mapId, floor, authHeader, { areaType });

  const curPoint = {
    type: "Point",
    coordinates: [location.userX, location.userY]
  };

  for (const area of areas) {
    const areaPoints = getAreaPoints(area);
    if (!areaPoints || areaPoints.length < 3) continue;
    const areaPolygon = {
      type: "Polygon",
      coordinates: [
        areaPoints.map(pt => [pt.x, pt.y]).concat([[areaPoints[0].x, areaPoints[0].y]])
      ]
    };
    if (
      floor === getAreaFloor(area) &&
      booleanPointInPolygon(curPoint, areaPolygon)
    ) {
      return area; // 첫 번째 포함 area 리턴
    }
  }
  return null;
};

exports.getAllUserLocationLogs = async (userId, page, limit) => {
  const from = (page - 1) * limit;

  try {
    const response = await osClient.search({
      index: 'user_locations_*',
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

    console.log(`[OpenSearch] Location logs retrieved for user ${userId}, page ${page}`);
    return response.body.hits.hits.map(hit => hit._source);
  } catch (err) {
    console.error('[OpenSearch] Error getting location logs:', err);
    throw err;
  }
};

exports.getLocationLogsByPeriod = async (userId, fromISO, toISO, page, limit) => {
  const from = (page - 1) * limit;
  const indexNames = getWeeksInRange(fromISO, toISO, 'user_locations'); // ["user_locations-2025-W20", ...]
  
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

    console.log(`[OpenSearch] Location logs from ${fromISO} to ${toISO} retrieved successfully for user ${userId}, page ${page} from valid indices:`, validIndexNames);
    return response.body.hits.hits.map(hit => hit._source);
  } catch (err) {
    console.error('[OpenSearch] Error retrieving location logs by period:', err.message);
    throw err;
  }
};