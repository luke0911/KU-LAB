const { osClient } = require("../../infra/db/openSearchClient");
const { withRedisClient } = require("../../infra/db/redisClient");

const INDEX_NAME = "button3_sensors";

exports.save = async (userId, payload) => {
  // battery 제거한 doc
  const doc = {
    userId,
    timestamp: new Date(),
    acceleration: payload.acceleration, // 문자열: "0.1,0.2,9.8"
    isWalking: payload.isWalking        // boolean
  };

  // OpenSearch 저장
  await osClient.index({
    index: INDEX_NAME,
    body: doc,
  });

  // Redis 캐싱 (1시간 유지)
  await withRedisClient((client) =>
    client.setEx(`recent-button3:${userId}`, 3600, JSON.stringify(doc))
  );

  return doc;
};

exports.findLatest = async (userId) => {
  // 캐시 먼저 확인
  const cached = await withRedisClient((client) =>
    client.get(`recent-button3:${userId}`)
  );
  if (cached) return JSON.parse(cached);

  // OpenSearch에서 최신 데이터 가져오기
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
