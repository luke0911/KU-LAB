// src/application/workerInfoService.js

const workerInfoRepository = require('../domain/repository/workerInfoRepository');

exports.createOrUpdateWorkerInfo = (data) => {
  return workerInfoRepository.save(data);
};

exports.getWorkerInfo = (userId) => {
  return workerInfoRepository.findById(userId);
};

exports.updateWorkerInfo = (userId, data) => {
  return workerInfoRepository.update(userId, data);
};

exports.deleteWorkerInfo = (userId) => {
  return workerInfoRepository.deleteById(userId);
};