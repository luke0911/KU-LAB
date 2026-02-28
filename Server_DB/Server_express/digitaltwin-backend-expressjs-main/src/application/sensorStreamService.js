const RealtimeSensorSample = require('../domain/model/realtimeSensorSample');
const sensorStreamRepository = require('../domain/repository/sensorStreamRepository');

/**
 * Parse, normalize, and persist an incoming realtime sensor frame.
 * @param {string} deviceId
 * @param {string} rawFrame - raw NDJSON line emitted from the Android client.
 * @returns {Promise<object|null>} Normalized JSON ready for broadcasting/subscription, or null if skipped.
 */
exports.ingestRealtimeFrame = async (deviceId, rawFrame) => {
  if (!rawFrame) {
    return null;
  }

  const sample = RealtimeSensorSample.fromNdjson(deviceId, rawFrame);

  await sensorStreamRepository.saveSample(sample);

  return sample.toJSON();
};

/**
 * Fetch the last realtime sample persisted for a device.
 * @param {string} deviceId
 * @returns {Promise<object|null>}
 */
exports.getLatestSample = async (deviceId) => {
  return await sensorStreamRepository.getLatestSample(deviceId);
};

/**
 * List deviceIds that have produced realtime data recently.
 * @returns {Promise<string[]>}
 */
exports.listActiveDevices = async () => {
  return await sensorStreamRepository.listActiveDevices();
};
