// button2Repository.js
const { osClient } = require("../../infra/db/openSearchClient");
const { withRedisClient } = require("../../infra/db/redisClient");

const INDEX_NAME = "button2_sensors";

exports.save = async (userId, payload) => {
  const doc = {
    userId,
    timestamp: new Date(),
    pressure: payload.pressure,
    gyro: payload.gyro, // string or object 통일 필요
    isWalking: payload.isWalking ? 1 : 0,
    floor: payload.floor,
  };

  await osClient.index({ index: INDEX_NAME, body: doc });

  await withRedisClient((client) =>
    client.setEx(`recent-button2:${userId}`, 3600, JSON.stringify(doc))
  );

  return doc;
};

exports.findLatest = async (userId) => {
  const cached = await withRedisClient((client) =>
    client.get(`recent-button2:${userId}`)
  );
  if (cached) return JSON.parse(cached);

  const result = await osClient.search({
    index: INDEX_NAME,
    body: {
      query: { match: { userId } },
      sort: [{ timestamp: { order: "desc" } }],
      size: 1,
    },
  });

  if (result.body.hits.hits.length > 0) {
    return result.body.hits.hits[0]._source;
  }
  return null;
};
