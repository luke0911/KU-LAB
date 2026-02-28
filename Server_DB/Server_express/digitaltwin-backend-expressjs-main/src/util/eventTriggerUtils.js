const { sendPushToUserAndAdmins } = require('./httpUtils');
const { withRedisClient } = require('../infra/db/redisClient');

/**
 * EventTrigger 타입 매핑
 */
const TRIGGER_TYPE_MAP = {
  'ENTRY': 'ENTER',
  'EXIT': 'EXIT', 
  'STAY': 'STAY'
};

/**
 * EventTrigger 조건을 확인하고 푸시 알림을 보내는 함수
 * @param {Object} matchedEventArea - 매칭된 EventArea 객체 (eventTriggers 포함)
 * @param {Object} transition - 상태 전이 정보
 * @param {string} userId - 사용자 ID
 * @param {string} curTime - 현재 시간
 * @param {string} prevEnterTime - 이전 진입 시간 (STAY 조건용)
 */
async function processEventTriggers(matchedEventArea, transition, userId, userRole, curTime, prevEnterTime) {
  if (!matchedEventArea || !matchedEventArea.eventTriggers) {
    return;
  }

  // transition.type이 'ENTER'일 때, TRIGGER_TYPE_MAP에서 key를 찾아서 변환
  const transitionType = Object.keys(TRIGGER_TYPE_MAP).find(key => TRIGGER_TYPE_MAP[key] === transition.type) || transition.type;
  console.log(`[EventTrigger] 트리거 처리: transitionType=${transitionType}, userRole=${userRole}`);
  
  // 해당 transition 타입에 맞는 트리거들 필터링
  const matchingTriggers = matchedEventArea.eventTriggers.filter(trigger => {
    return trigger.isActive &&
           trigger.triggerType === transitionType &&
           Array.isArray(trigger.targetUserRoles) &&
           trigger.targetUserRoles.includes(userRole);
  });

  if (matchingTriggers.length === 0) {
    return;
  }

  // STAY 타입인 경우 체류 시간 계산
  let stayDuration = null;
  if (transitionType === 'STAY' && prevEnterTime) {
    const enterTime = new Date(prevEnterTime);
    const currentTime = new Date(curTime);
    stayDuration = (currentTime.getTime() - enterTime.getTime()) / 1000; // 초 단위
  }

  // 각 트리거에 대해 푸시 알림 전송
  for (const trigger of matchingTriggers) {
    // STAY 타입이고 delay가 설정된 경우, 체류 시간이 delay를 초과했는지 확인
    console.log(`[EventTrigger] 트리거 처리: trigger.isActive=${trigger.isActive}, trigger.triggerType=${trigger.triggerType}, trigger.delay=${trigger.delay}, trigger.targetUserRoles=${trigger.targetUserRoles}, userRole=${userRole}`);
    if (transitionType === 'STAY' && trigger.delay) {
      console.log(`[EventTrigger] 트리거 처리: stayDuration=${stayDuration}, trigger.delay=${trigger.delay}`);
      if (!stayDuration || stayDuration < trigger.delay / 1000) { // delay는 ms 단위
        continue; // 조건을 만족하지 않으면 스킵
      }
    }

    if (transitionType === 'STAY') {
      // Redis 키 생성
      const redisKey = `stayPushSent:${userId}:${matchedEventArea.eventAreaId}:${trigger.triggerId}`;

      // 이미 전송했는지 체크
      const alreadySent = await withRedisClient(async (client) => {
        return await client.get(redisKey);
      });
      if (alreadySent) {
        console.log(`[EventTrigger] STAY 푸시 이미 전송됨, 스킵: ${redisKey}`);
        continue; // 중복 전송 방지
      }

      // 전송 후 상태 저장 (1시간 유지)
      await withRedisClient(async (client) => {
        await client.set(redisKey, '1', { EX: 3600 });
      });
    }

    // 푸시 알림 전송
    await sendPushToUserAndAdmins(
      userId,
      trigger.eventMessageType,
      trigger.eventMessage
    );

    console.log(`[EventTrigger] 트리거 실행: ${trigger.triggerName}, 타입: ${transitionType}, 사용자: ${userId}`);
  }
}

module.exports = {
  processEventTriggers,
  TRIGGER_TYPE_MAP
}; 