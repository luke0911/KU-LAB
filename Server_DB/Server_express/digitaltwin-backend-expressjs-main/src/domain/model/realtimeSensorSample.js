const VERSION = 1;
const FLOAT_FIELDS = [
  'pitch', 'roll', 'yaw',
  'rot_x', 'rot_y', 'rot_z', 'rot_w',
  'gyro_x', 'gyro_y', 'gyro_z',
  'accel_x', 'accel_y', 'accel_z',
  'linear_x', 'linear_y', 'linear_z',
  'light', 'pressure', 'proximity',
  'heading', 'MM',
  'steplength',
  'magx', 'magy', 'magz',
];

const INTEGER_FIELDS = [
  'statereal', 'statetmp',
  'totalstep', 'isstep', 'stateflag',
  'statequeue0', 'statequeue1', 'statequeue2', 'statequeue3', 'statequeue4',
];

function parseNumber(value) {
  if (value === undefined || value === null) return null;
  if (typeof value === 'number') {
    return Number.isFinite(value) ? value : null;
  }
  const num = Number(value);
  return Number.isFinite(num) ? num : null;
}

function parseInteger(value) {
  const numeric = parseNumber(value);
  if (numeric === null) return null;
  return Math.trunc(numeric);
}

class RealtimeSensorSample {
  constructor({ deviceId, payload, receivedAt = Date.now() }) {
    if (!deviceId || typeof deviceId !== 'string') {
      throw new Error('deviceId (string) is required for realtime sensor samples');
    }
    if (!payload || typeof payload !== 'object') {
      throw new Error('Realtime sensor payload must be an object');
    }

    this.deviceId = deviceId;
    this.receivedAt = receivedAt;
    this.payload = payload;
    this.normalized = this.normalize(payload);
  }

  static fromNdjson(deviceId, rawJsonLine) {
    let parsed;
    try {
      parsed = JSON.parse(rawJsonLine);
    } catch (err) {
      const error = new Error(`Invalid JSON payload from realtime stream: ${err.message}`);
      error.cause = err;
      throw error;
    }
    return new RealtimeSensorSample({ deviceId, payload: parsed });
  }

  normalize(payload) {
    const now = this.receivedAt;
    const normalized = {
      version: VERSION,
      deviceId: this.deviceId,
      reportedDeviceId: typeof payload.deviceId === 'string' ? payload.deviceId.trim() || null : null,
      serverReceivedAt: now,
      serverReceivedAtIso: new Date(now).toISOString(),
    };

    if (
      normalized.reportedDeviceId &&
      normalized.reportedDeviceId !== this.deviceId
    ) {
      normalized.deviceIdMismatch = true;
    }

    const timestamp = parseInteger(payload.timestamp);
    normalized.timestamp = timestamp;
    normalized.timestampIso = Number.isInteger(timestamp)
      ? new Date(timestamp).toISOString()
      : null;

    for (const field of FLOAT_FIELDS) {
      normalized[field] = parseNumber(payload[field]);
    }

    for (const field of INTEGER_FIELDS) {
      normalized[field] = parseInteger(payload[field]);
    }

    normalized.isStep = normalized.isstep === null ? null : normalized.isstep === 1;
    normalized.stateFlag = normalized.stateflag === null ? null : normalized.stateflag === 1;

    const stateQueue = [];
    for (let i = 0; i < 5; i++) {
      stateQueue.push(normalized[`statequeue${i}`]);
    }
    normalized.stateQueue = stateQueue;

    return normalized;
  }

  toJSON() {
    return { ...this.normalized };
  }
}

module.exports = RealtimeSensorSample;
