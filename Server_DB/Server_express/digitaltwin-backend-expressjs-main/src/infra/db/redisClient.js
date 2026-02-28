const redis = require('redis');
const { redis: redisConfig } = require('../../config/env');
const { createPool } = require('generic-pool');

const factory = {
  create: async () => {
    console.log('Creating new Redis client...');
    const client = redis.createClient({
      url: `redis://${redisConfig.username}:${redisConfig.password}@${redisConfig.host}:${redisConfig.port}`,
      socket: {
        reconnectStrategy: retries => Math.min(retries * 50, 500),
        connectTimeout: 10000,
      }
    });

    client.on('error', (err) => {
      console.error('Redis Client Error:', err);
    });
    
    client.on('end', () => {
      console.warn('Redis client disconnected.');
    });
    
    client.on('reconnecting', () => {
      console.log('Redis client trying to reconnect...');
    });

    console.log('Redis client created, connecting...');
    await client.connect();
    console.log('Redis client connected!');
    return client;
  },
  destroy: async (client) => {
    await client.quit();
  }
};

const redisPool = createPool(factory, {
  max: 10, // 최대 연결 수
  min: 1,
  maxRetriesPerRequest: 2,
  idleTimeoutMillis: 30000,
  validate: async (client) => {
    try {
      await client.ping();
      return true;
    } catch {
      return false;
    }
  }
});

async function connectRedis() {
  
  const client = await redisPool.acquire();
  try {
    await client.ping();
    console.log('Redis connected!!');
  } catch (err) {
    console.error('Redis connection test failed:', err);
    // 문제 있는 커넥션은 폐기
    await redisPool.destroy(client);
    throw err;
  } finally {
    await redisPool.release(client);
  }
}

async function withRedisClient(fn) {
  const client = await redisPool.acquire();
  try {
    return await fn(client);
  } catch (err) {
    console.error('[Redis] Error in withRedisClient:', err);
    // 문제 있는 커넥션은 폐기
    await redisPool.destroy(client);
    throw err;
  } finally {
    await redisPool.release(client);
  }
}

module.exports = { withRedisClient, connectRedis, redisPool };
