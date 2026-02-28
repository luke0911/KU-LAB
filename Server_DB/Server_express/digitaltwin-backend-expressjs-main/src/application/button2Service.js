const button2Repository = require("../domain/repository/button2Repository");

exports.saveButton2Data = async (userId, payload) => {
  return await button2Repository.save(userId, payload);
};

exports.getLatestButton2Data = async (userId) => {
  return await button2Repository.findLatest(userId);
};
