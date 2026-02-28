const axios = require('axios');
const config = require('../config/env');

/**
 * Spring API로 푸시 알림을 보내는 함수
 * @param {string} userId - 사용자 ID
 * @param {string} title - 알림 제목 (eventMessageType)
 * @param {string} body - 알림 내용 (eventMessage)
 */
async function sendPushToUserAndAdmins(userId, title, body) {
  try {
    const response = await axios.post(`${config.host}/spring/api/push/user-and-admins`, {
      userId: userId,
      title: title,
      body: body
    }, {
      headers: {
        'Content-Type': 'application/json',
        'X-Internal-Api-Key': config.internalApiKey
      },
      timeout: 5000 // 5초 타임아웃
    });
    
    console.log(`[Push] 푸시 알림 전송 성공: userId=${userId}, title=${title} body=${body}`);
    return response;
  } catch (error) {
    console.error(`[Push] 푸시 알림 전송 실패: userId=${userId}, title=${title} body=${body}`, error.message);
    // 에러가 발생해도 전체 프로세스는 계속 진행
    return null;
  }
}

module.exports = {sendPushToUserAndAdmins};