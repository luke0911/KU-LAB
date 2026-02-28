const button3Repository = require("../domain/repository/button3Repository");

exports.saveButton3Data = async (userId, payload) => {
  // Repository로 전달 (battery 제거는 repository에서 처리됨)
  return await button3Repository.save(userId, payload);
};

exports.getLatestButton3Data = async (userId) => {
  return await button3Repository.findLatest(userId);
};
