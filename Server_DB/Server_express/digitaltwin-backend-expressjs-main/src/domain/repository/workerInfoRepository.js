// src/domain/repository/workerInfoRepository.js

const adapter = require('../../infra/db/repository/workerInfoAdapter');
const WorkerInfo = require('../model/workerInfo');

// 데이터 생성 또는 전체 업데이트
exports.save = async (workerInfoData) => {
  const workerInfo = new WorkerInfo(workerInfoData);
  await adapter.saveToOpenSearch(workerInfo);
  await adapter.saveToRedis(workerInfo);
  return workerInfo.toJSON();
};

// 데이터 조회
exports.findById = async (userId) => {
  return await adapter.findById(userId);
};

// 데이터 삭제
exports.deleteById = async (userId) => {
  await adapter.deleteById(userId);
  return { status: 'deleted', userId };
};

// 데이터 부분 업데이트
exports.update = async (userId, updateData) => {
  const existingData = await adapter.findById(userId);
  if (!existingData) {
    return null; // 업데이트할 데이터가 없음
  }

  // 기존 데이터에 새로운 데이터를 덮어쓰기
  const updatedData = { ...existingData, ...updateData };
  
  const workerInfo = new WorkerInfo(updatedData);
  await adapter.saveToOpenSearch(workerInfo);
  await adapter.saveToRedis(workerInfo);
  return workerInfo.toJSON();
};