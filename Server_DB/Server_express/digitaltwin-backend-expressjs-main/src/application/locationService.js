const Location = require('../domain/model/location');
const locationRepository = require('../domain/repository/locationRepository');

exports.saveLocation = async (data, authHeader) => {
  const location = new Location(data);
  return await locationRepository.save(location, data.role, authHeader);
};

exports.getLatestLocationbyUserId = async (userId) => {
  return await locationRepository.getLatestLocationbyUserId(userId);
};

exports.getAllRecentLocations = async () => {
  return await locationRepository.getAllRecentLocations();
}

exports.getUserLocationLogs = async (userId, page, limit) => {
  return await locationRepository.getAllUserLocationLogsByUserId(userId, page, limit);
};

exports.getLocationLogsByPeriod = async (userId, from, to, page, limit) => {
  return await locationRepository.getLocationLogsByPeriod(userId, from, to, page, limit);
};