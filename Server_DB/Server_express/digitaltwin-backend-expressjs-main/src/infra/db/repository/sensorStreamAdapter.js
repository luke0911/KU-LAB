const { withRedisClient } = require('../redisClient');

const KEY_PREFIX = 'sensor-stream';
const SAMPLE_TTL_SECONDS = 60;

function buildKey(deviceId) {
  return `${KEY_PREFIX}:${deviceId}`;
}

exports.saveRealtimeSample = async (sample) => {
  const payload = sample.toJSON();
  const deviceId = payload.deviceId;
  if (!deviceId) {
    throw new Error('Realtime sample is missing deviceId');
  }
  const json = JSON.stringify(payload);
  const key = buildKey(deviceId);

  await withRedisClient(async (client) => {
    await client.set(key, json, { EX: SAMPLE_TTL_SECONDS });
  });
};

exports.getLatestRealtimeSample = async (deviceId) => {
  if (!deviceId) return null;
  const key = buildKey(deviceId);

  return await withRedisClient(async (client) => {
    const raw = await client.get(key);
    if (!raw) return null;
    try {
      return JSON.parse(raw);
    } catch (err) {
      console.error('[Redis] Failed to parse realtime sensor sample for key:', key, err);
      return null;
    }
  });
};

exports.listActiveRealtimeDevices = async () => {
  return await withRedisClient(async (client) => {
    const devices = new Set();
    let cursor = '0';

    do {
      const reply = await client.scan(cursor, {
        MATCH: `${KEY_PREFIX}:*`,
        COUNT: 100,
      });
      cursor = reply.cursor;
      for (const key of reply.keys) {
        const deviceId = key.slice(KEY_PREFIX.length + 1);
        if (deviceId) {
          devices.add(deviceId);
        }
      }
    } while (cursor !== '0');

    return Array.from(devices);
  });
};
