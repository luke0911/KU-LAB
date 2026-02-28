const SensorData = require('../domain/model/sensorData');
const sensorRepository = require('../domain/repository/sensorRepository');

exports.saveSensorData = async (data) => {
  const sensorData = new SensorData(data);
  return await sensorRepository.save(sensorData);
};

exports.getAllUserSensorLogs = async (userId, page, limit) => {
  return await sensorRepository.getAllUserSensorLogs(userId, page, limit);
}

exports.getSensorLogsByPeriod = async (userId, from, to, page, limit) => {
  return await sensorRepository.getSensorLogsByPeriod(userId, from, to, page, limit);
};

exports.getLatestSensorLogByUserId = async (userId) => {
  return await sensorRepository.getLatestSensorLogByUserId(userId);
}

exports.getRecentSensorLog = async (userId) => {
  return await sensorRepository.getRecentSensorLog(userId);
}