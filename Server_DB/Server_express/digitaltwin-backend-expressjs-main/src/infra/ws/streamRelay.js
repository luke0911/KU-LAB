const WebSocket = require('ws');
const { URL } = require('url');

const subscriberMap = new Map(); // deviceId -> Set<WebSocket>
const wildcardSubscribers = new Set(); // Subscribers listening to every device

/**
 * Initialize WebSocket relay server.
 * @param {import('http').Server} server
 * @param {(Function|object)} [options]
 * @param {(msg: object) => void} [options.sendToMaster] callback to bubble messages to the cluster master
 * @param {(args: { deviceId: string, raw: string, socket: WebSocket }) => (Promise<object|string|null>|object|string|null)} [options.onPublisherFrame]
 */
function initStreamRelay(server, options = {}) {
  let sendToMaster = null;
  let onPublisherFrame = null;

  if (typeof options === 'function') {
    sendToMaster = options;
  } else if (options && typeof options === 'object') {
    sendToMaster = options.sendToMaster || null;
    onPublisherFrame = options.onPublisherFrame || null;
  }

  const wss = new WebSocket.Server({ server, path: '/express/stream' });

  wss.on('connection', (socket, request) => {
    const { searchParams } = new URL(request.url, `http://${request.headers.host}`);
    const role = (searchParams.get('role') || 'subscriber').toLowerCase();
    const deviceId = searchParams.get('deviceId');

    if (role === 'publisher') {
      if (!deviceId) {
        socket.close(1008, 'deviceId query param required for publishers');
        return;
      }
      socket.isPublisher = true;
      socket.deviceId = deviceId;

      socket.on('message', async (data) => {
        const payloadText = data.toString();
        const frames = payloadText.split(/\r?\n/);

        for (const frame of frames) {
          const raw = frame.trim();
          if (!raw) continue;

          let toBroadcast = raw;

          if (typeof onPublisherFrame === 'function') {
            try {
              const processed = await onPublisherFrame({ deviceId, raw, socket });
              if (!processed) {
                continue;
              }
              toBroadcast = typeof processed === 'string' ? processed : JSON.stringify(processed);
            } catch (err) {
              console.error('[WS relay] Failed to process realtime payload:', err);
              continue;
            }
          }

          broadcastLocal(deviceId, toBroadcast);

          if (typeof sendToMaster === 'function') {
            try {
              sendToMaster({ type: 'sensor_stream', deviceId, payload: toBroadcast });
            } catch (err) {
              console.error('[WS relay] Failed to escalate payload to master:', err);
            }
          }
        }
      });
    } else {
      const targetId = deviceId && deviceId.trim() !== '' ? deviceId : '*';
      attachSubscriber(socket, targetId);
      safeSend(socket, JSON.stringify({ type: 'ready', deviceId: targetId }));
    }

    socket.on('close', () => detach(socket));
    socket.on('error', () => detach(socket));
  });

  return wss;
}

function attachSubscriber(socket, deviceId) {
  socket.isPublisher = false;
  socket.subscribedDeviceId = deviceId;

  if (deviceId === '*') {
    wildcardSubscribers.add(socket);
    return;
  }

  if (!subscriberMap.has(deviceId)) {
    subscriberMap.set(deviceId, new Set());
  }
  subscriberMap.get(deviceId).add(socket);
}

function detach(socket) {
  if (socket.isPublisher) {
    return;
  }

  const deviceId = socket.subscribedDeviceId;
  if (!deviceId) {
    return;
  }

  if (deviceId === '*') {
    wildcardSubscribers.delete(socket);
    return;
  }

  const bucket = subscriberMap.get(deviceId);
  if (!bucket) return;

  bucket.delete(socket);
  if (bucket.size === 0) {
    subscriberMap.delete(deviceId);
  }
}

function broadcastLocal(deviceId, payload) {
  const message = typeof payload === 'string' ? payload : JSON.stringify(payload);

  const byDevice = subscriberMap.get(deviceId);
  if (byDevice) {
    for (const client of byDevice) {
      safeSend(client, message);
    }
  }

  for (const client of wildcardSubscribers) {
    safeSend(client, message);
  }
}

function handleMasterBroadcast(message) {
  if (!message || message.type !== 'sensor_stream') {
    return;
  }
  const { deviceId, payload } = message;
  if (!deviceId || payload === undefined) {
    return;
  }
  broadcastLocal(deviceId, payload);
}

function safeSend(socket, data) {
  if (socket.readyState === WebSocket.OPEN) {
    try {
      socket.send(data);
    } catch (err) {
      console.error('[WS relay] Failed to send to subscriber:', err);
    }
  }
}

module.exports = {
  initStreamRelay,
  handleMasterBroadcast,
};
