const locationAdapter = require('../../infra/db/repository/locationAdapter');
const poiStayAdapter = require('../../infra/db/repository/poiStayAdapter');
const eventAreaStayAdapter = require('../../infra/db/repository/eventAreaStayAdapter');
const userProfileAdapter = require('../../infra/db/repository/userProfileAdapter');
const poiStay = require('../model/poiStay');
const eventAreaStay = require('../model/eventAreaStay');
const { processEventTriggers } = require('../../util/eventTriggerUtils');
const { withRedisClient } = require('../../infra/db/redisClient');

/**
 * prev: 이전 위치 상태 객체 (Location)
 * cur: 현재 위치 상태 객체 (Location)
 * matchedArea: 현재 포함된 Area(Poi나 EventArea)
 * curTime: 현재 시간
 * options: {
 *   prevAreaKey: string, // prev/matchedArea 참조할 키 ('matchedPoi', 'matchedEventArea')
 *   areaIdKey: string, // matchedArea의 id 필드 키 ('poiId', 'areaId')
 *   enterTimeKey: string, // Location에서의 진입 시간 필드 ('poiEnterTime', 'eventAreaEnterTime')
 * }
 */
function getAreaTransition(prev, cur, matchedArea, curTime, options) {
  const {
    prevAreaKey,
    areaIdKey,
    enterTimeKey,
    areaName,
    log
  } = options;

  const logger = typeof log === 'function' ? log : console.log;
  const prevArea = prev ? prev[prevAreaKey] : null;
  const prevEnterTime = prev ? prev[enterTimeKey] : null;
  const curAreaId = matchedArea ? matchedArea[areaIdKey] : null;

  // 1. 초기상태 (prev 없음)
  if (!prev) {
    if (curAreaId != null) {
      logger(`[${areaName ?? 'Area'}][${cur.userId}] 상태: ENTER (최초 진입, areaId: ${curAreaId}) at ${curTime}`);
      return { type: 'ENTER', areaId: curAreaId, enterTime: curTime };
    } else {
      logger(`[${areaName ?? 'Area'}][${cur.userId}] 상태: OUTSIDE (최초 위치) at ${curTime}`);
      return { type: 'OUTSIDE', areaId: null, enterTime: null };
    }
  }

  // 2. OUTSIDE → ENTER (직전 area 없음, 현재 있음)
  if (!prevArea && curAreaId != null) {
    logger(`[${areaName ?? 'Area'}][${cur.userId}] 상태: ENTER (OUTSIDE → areaId: ${curAreaId}) at ${curTime}`);
    return { type: 'ENTER', areaId: curAreaId, enterTime: curTime };
  }

  // 3. OUTSIDE → OUTSIDE (계속 밖)
  if (!prevArea && curAreaId == null) {
    logger(`[${areaName ?? 'Area'}][${cur.userId}] 상태: OUTSIDE (계속 OUTSIDE) at ${curTime}`);
    return { type: 'OUTSIDE', areaId: null, enterTime: null };
  }

  // 4. 같은 area에 머무름 (STAY)
  if (prevArea != null && curAreaId != null && prevArea === curAreaId) {
    logger(`[${areaName ?? 'Area'}][${cur.userId}] 상태: STAY (areaId: ${curAreaId}) at ${curTime}`);
    return { type: 'STAY', areaId: curAreaId, enterTime: prevEnterTime };
  }

  // 5. area에서 나감 (EXIT) - 현재 area 없음
  if (prevArea != null && curAreaId == null) {
    logger(`[${areaName ?? 'Area'}][${cur.userId}] 상태: EXIT (areaId: ${prevArea}) at ${curTime}`);
    return {
      type: 'EXIT',
      areaId: null,
      enterTime: null,
      logInfo: {
        mapId: cur.mapId,
        userId: cur.userId,
        prevArea,
        prevEnterTime,
        curTime,
      },
    };
  }

  // 6. area 간 이동 (MOVE) - area 변경
  if (prevArea != null && curAreaId != null && prevArea !== curAreaId) {
    logger(`[${areaName ?? 'Area'}][${cur.userId}] 상태: MOVE (${prevArea} → ${curAreaId}) at ${curTime}`);
    return {
      type: 'MOVE',
      areaId: curAreaId,
      enterTime: curTime,
      logInfo: {
        mapId: cur.mapId,
        userId: cur.userId,
        prevArea,
        prevEnterTime,
        curTime,
      },
    };
  }

  // 7. 예외 fallback
  logger(`[${areaName ?? 'Area'}][${cur.userId}] 상태: OUTSIDE (예외 fallback) at ${curTime}`);
  return { type: 'OUTSIDE', areaId: null, enterTime: null };
}

/**
 * transitionType: 상태 전이 타입 (EXIT/MOVE 등)
 * logInfo: 로그 정보
 * areaType: 'poi' or 'eventArea' 등
 * adapters: {
 *   fromChange: 로그 생성 함수
 *   saveToOpenSearch: 저장 함수
 * }
 */
async function handleAreaStayLogIfNeeded(transitionType, logInfo, areaType, adapters) {
  if (['EXIT', 'MOVE'].includes(transitionType) && logInfo) {
    const log = adapters.fromChange(logInfo);
    await adapters.saveToOpenSearch(log);

    const userId = logInfo.userId;
    const eventAreaId = logInfo.prevArea;

    if (areaType === 'eventArea' && userId && eventAreaId) {
      await withRedisClient(async (client) => {
        const keysPattern = `stayPushSent:${userId}:${eventAreaId}:*`;

        // Redis 키 조회 (keys는 프로덕션에선 조심)
        const keys = await client.keys(keysPattern);

        if (keys.length > 0) {
          await client.del(...keys);
          console.log(`[handleAreaStayLogIfNeeded] STAY 푸시 전송 기록 초기화: ${keys}`);
        }
      });
    }
  }
}

exports.save = async (location, userRole, authHeader) => {
  const curTime = location.timestamp;
  const userId = location.userId;

  // 판정 (공통화)
  // POI
  const matchedPoi = await locationAdapter.findAreaIncludingLocation(
    location,
    authHeader,
    'poi',
    area => area.poiPoints,
    area => area.poiFloor
  );

  // EventArea
  const matchedEventArea = await locationAdapter.findAreaIncludingLocation(
    location,
    authHeader,
    'eventArea',
    area => area.eventAreaPoints,   // eventAreaPoints 등 필드명에 맞게!
    area => area.eventAreaFloor     // eventAreaFloor 등 필드명에 맞게!
  );
  const latestLocation = await locationAdapter.getRecentLocationByUserId(userId);

  // 상태 전이 (공통 함수로)
  const poiTransition = getAreaTransition(
    latestLocation,
    location,
    matchedPoi,
    curTime,
    {
      prevAreaKey: 'matchedPoi',
      areaIdKey: 'poiId',
      enterTimeKey: 'poiEnterTime',
      areaName: 'POI',
    }
  );
  const eventAreaTransition = getAreaTransition(
    latestLocation,
    location,
    matchedEventArea,
    curTime,
    {
      prevAreaKey: 'matchedEventArea',
      areaIdKey: 'eventAreaId',
      enterTimeKey: 'eventAreaEnterTime',
      areaName: 'Event Area',
    }
  );

  // 체류 로그 저장 (공통 함수로)
  await handleAreaStayLogIfNeeded(poiTransition.type, poiTransition.logInfo, 'poi', {
    fromChange: poiStayAdapter.fromPoiChangeAdapter,
    saveToOpenSearch: poiStayAdapter.saveToOpenSearch,
  });
  await handleAreaStayLogIfNeeded(eventAreaTransition.type, eventAreaTransition.logInfo, 'eventArea', {
    fromChange: eventAreaStayAdapter.fromEventAreaChangeAdapter,
    saveToOpenSearch: eventAreaStayAdapter.saveToOpenSearch,
  });

  // EventTrigger 처리 (matchedEventArea가 존재할 때)
  if (matchedEventArea) {
    await processEventTriggers(
      matchedEventArea,
      eventAreaTransition,
      userId,
      userRole,
      curTime,
      latestLocation ? latestLocation.eventAreaEnterTime : null
    );
  }

  // Location 객체에 상태 반영
  location.matchedPoi = poiTransition.areaId;
  location.poiEnterTime = poiTransition.enterTime;
  location.matchedEventArea = eventAreaTransition.areaId;
  location.eventAreaEnterTime = eventAreaTransition.enterTime;

  // --- 유저 프로필 정보 조회 ---
  const userProfile = await userProfileAdapter.getUserProfileByUserId(userId);

  if (userProfile) {
    location.userName = userProfile.userName;
    location.ageRange = userProfile.ageRange;
    location.gender = userProfile.gender;
  } else {
    // 기본값 처리
    location.userName = null;
    location.ageRange = null;
    location.gender = null;
  }

  await locationAdapter.saveToRedis(location);
  await locationAdapter.saveToOpenSearch(location);

  return { status: 'ok' };
};

exports.getLatestLocationbyUserId = async (userId) => {
  return await locationAdapter.getLatestLocationbyUserId(userId);
}

exports.getAllRecentLocations = async () => {
  return await locationAdapter.getAllRecentLocations();
}

exports.getAllUserLocationLogsByUserId = async (userId, page, limit) => {
  return await locationAdapter.getAllUserLocationLogs(userId, page, limit);
};

exports.getLocationLogsByPeriod = async (userId, fromISO, toISO, page, limit) => {
  return await locationAdapter.getLocationLogsByPeriod(userId, fromISO, toISO, page, limit);
};
