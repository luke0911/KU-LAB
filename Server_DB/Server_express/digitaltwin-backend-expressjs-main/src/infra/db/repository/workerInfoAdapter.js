// src/infra/db/repository/workerInfoAdapter.js

const { withRedisClient } = require('../redisClient');
const { osClient } = require('../openSearchClient');

const INDEX_NAME = 'worker_info'; // OpenSearch 인덱스 이름
const REDIS_PREFIX = 'worker-info:'; // Redis 키 접두사

// OpenSearch에 데이터 저장/수정
exports.saveToOpenSearch = async (workerInfo) => {
  await osClient.index({
    index: INDEX_NAME,
    id: workerInfo.userId, // UUID를 문서 ID로 사용
    body: workerInfo.toJSON(),
    refresh: true, // 즉시 검색 가능하도록 설정
  });
};

// Redis에 최신 정보 캐싱
exports.saveToRedis = async (workerInfo) => {
  const key = `${REDIS_PREFIX}${workerInfo.userId}`;
  const value = JSON.stringify(workerInfo.toJSON());
  await withRedisClient(async (client) => {
    await client.set(key, value, { EX: 86400 }); // 24시간 캐시
  });
};

// ID로 정보 조회 (Redis -> OpenSearch 순)
exports.findById = async (userId) => {
  const key = `${REDIS_PREFIX}${userId}`;
  const cached = await withRedisClient(async (client) => client.get(key));
  if (cached) return JSON.parse(cached);

  try {
    const response = await osClient.get({
      index: INDEX_NAME,
      id: userId,
    });
    return response.body._source;
  } catch (error) {
    if (error.meta.statusCode === 404) return null;
    throw error;
  }
};

// ID로 정보 삭제
exports.deleteById = async (userId) => {
  // OpenSearch에서 삭제
  await osClient.delete({
    index: INDEX_NAME,
    id: userId,
    refresh: true,
  });

  // Redis 캐시에서도 삭제
  const key = `${REDIS_PREFIX}${userId}`;
  await withRedisClient(async (client) => client.del(key));
};