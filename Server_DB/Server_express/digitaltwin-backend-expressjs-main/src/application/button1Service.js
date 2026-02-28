const button1Repository = require("../domain/repository/button1Repository");

exports.saveButton1Data = async (userId, payload) => {
  return await button1Repository.save(userId, payload);
};

exports.getLatestButton1Data = async (userId) => {
  return await button1Repository.findLatest(userId);
};
