
const poiStayRepository = require('../domain/repository/poiStayRepository');

exports.getLatestPoiStayLogbyUserId = async (userId) => {
    return await poiStayRepository.getLatestPoiStayLogbyUserId(userId);
}
  
exports.getAllPoiStayLogs = async (userId, page, limit) => {
    return await poiStayRepository.getAllPoiStayLogs(userId, page, limit);
};
  
exports.getPoiStayLogsByPeriod = async (userId, fromISO, toISO, page, limit) => {
    return await poiStayRepository.getPoiStayLogsByPeriod(userId, fromISO, toISO, page, limit);
};
  