const adapter = require('../../infra/db/repository/sensorAdapter');
const userProfileAdapter = require('../../infra/db/repository/userProfileAdapter')

exports.save = async (sensorData) => {

    // --- 유저 프로필 정보 조회 ---
    const userProfile = await userProfileAdapter.getUserProfileByUserId(sensorData.userId);

    if (userProfile) {
      sensorData.userName = userProfile.userName;
    } else {
      // 기본값 처리
      sensorData.userName = null;
    }

  await adapter.saveToOpenSearch(sensorData);
  await adapter.saveToRedis(sensorData);
  return { status: 'ok' };
};

exports.getAllUserSensorLogs = async (userId, page, limit) => {
  return await adapter.getAllUserSensorLogs(userId, page, limit);
}

exports.getSensorLogsByPeriod = async (userId, fromISO, toISO, page, limit) => {
  return await adapter.getSensorLogsByPeriod(userId, fromISO, toISO, page, limit);
};

exports.getLatestSensorLogByUserId = async (userId) => {
  return await adapter.getLatestSensorLogByUserId(userId);
}

exports.getRecentSensorLog = async (userId) => {
  return await adapter.getRecentSensorLog(userId);
}