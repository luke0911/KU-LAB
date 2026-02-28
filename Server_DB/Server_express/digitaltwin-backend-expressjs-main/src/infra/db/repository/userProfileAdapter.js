const { withRedisClient } = require('../redisClient');

exports.getUserProfileByUserId = async (userId) => {
    return await withRedisClient(async (client) => {
      const key = `user-profile:${userId}`;
      console.log(key);
      const rawData = await client.get(key);
      console.log(rawData);
      if (rawData) {
        return JSON.parse(rawData);
      } else {
        return null; // 위치 정보가 없을 때 null 반환
      }
    });
  };