const adapter = require('../../infra/db/repository/sensorStreamAdapter');

exports.saveSample = async (sample) => {
  await adapter.saveRealtimeSample(sample);
};

exports.getLatestSample = async (deviceId) => {
  if (!deviceId) {
    return null;
  }
  return await adapter.getLatestRealtimeSample(deviceId);
};

exports.listActiveDevices = async () => {
  return await adapter.listActiveRealtimeDevices();
};
