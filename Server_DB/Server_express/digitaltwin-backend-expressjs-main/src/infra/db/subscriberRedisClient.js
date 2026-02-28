const redis = require('redis');
const { redis: redisConfig } = require('../../config/env');

const subscriber = redis.createClient({
  url: `redis://${redisConfig.username}:${redisConfig.password}@${redisConfig.host}:${redisConfig.port}`,
  socket: {
    reconnectStrategy: retries => Math.min(retries * 50, 500),
    connectTimeout: 10000,
  }
});

subscriber.on('error', (err) => {
  console.error('[Redis:Subscriber] Error:', err);
});

subscriber.on('connect', () => {
  console.log('[Redis:Subscriber] Connected');
});

module.exports = { subscriber };