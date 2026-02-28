const poiStayAdapter = require('../../infra/db/repository/poiStayAdapter');

exports.getLatestPoiStayLogbyUserId = async (userId) => {
    return await poiStayAdapter.getLatestPoiStayLogbyUserId(userId);
}
  
exports.getAllPoiStayLogs = async (userId, page, limit) => {
    return await poiStayAdapter.getAllPoiStayLogs(userId, page, limit);
};
  
exports.getPoiStayLogsByPeriod = async (userId, fromISO, toISO, page, limit) => {
    return await poiStayAdapter.getPoiStayLogsByPeriod(userId, fromISO, toISO, page, limit);
};
  