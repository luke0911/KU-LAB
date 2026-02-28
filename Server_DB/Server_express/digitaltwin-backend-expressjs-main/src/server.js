// server.js

// 로그 한국 현재 시간 표시
const origLog = console.log;
console.log = function (...args) {
  const kst = (() => {
    const date = new Date();
    date.setHours(date.getHours() + 9);
    return date.toISOString().replace('T', ' ').substring(0, 19);
  })();

  origLog.apply(console, [`[${kst}]`, ...args]);
};

const cluster = require('cluster');
const os = require('os');
const { connectRedis, redisPool } = require('./infra/db/redisClient');
const { pingOpenSearch } = require('./infra/db/openSearchClient');
const app = require('./app');
const { getAllRecentLocations } = require('./infra/db/repository/locationAdapter');
const http = require('http');
const { initStreamRelay, handleMasterBroadcast } = require('./infra/ws/streamRelay');
const sensorStreamService = require('./application/sensorStreamService');

const PORT = process.env.PORT || 3000;

async function startServer() {
  try {
    console.log('Connect Redis');
    await connectRedis();

    console.log('Ping OpenSearch');
    await pingOpenSearch();

    const httpServer = http.createServer(app);

    initStreamRelay(httpServer, {
      sendToMaster: (message) => {
        if (process.send) {
          process.send(message);
        }
      },
      onPublisherFrame: async ({ deviceId, raw }) => {
        try {
          return await sensorStreamService.ingestRealtimeFrame(deviceId, raw);
        } catch (err) {
          console.error('[Realtime] Failed to ingest frame:', err);
          return null;
        }
      },
    });

    httpServer.listen(PORT, () => {
      console.log(`Worker ${process.pid} listening on port ${PORT}`);
    });
  } catch (err) {
    console.error('Startup failed:', err);
    // 예외 발생 시 graceful shutdown 유도
    process.kill(process.pid, 'SIGTERM');
  }
}

let isShuttingDown = false;

if (cluster.isMaster) {
  const numCPUs = os.cpus().length; // 서버 CPU 갯수
  // const numWorkers = 4;
  console.log(`Master ${process.pid} is running`);
  console.log(`Starting ${numCPUs} workers...`);

  for (let i = 0; i < numCPUs; i++) {
    cluster.fork();
  }

  cluster.on('exit', (worker, code, signal) => {
    console.log(`Worker ${worker.process.pid} died.`);

    if (!isShuttingDown) {
      console.log('Restarting worker...');
      setTimeout(() => {
        cluster.fork();
      }, 1000);
    }
  });

  const shutdownMaster = async () => {
    if (isShuttingDown) return;
    console.log('Gracefully shutting down master...');
    isShuttingDown = true;

    for (const id in cluster.workers) {
      cluster.workers[id].process.kill('SIGTERM');
    }

    // 5초 후 강제 종료 (워커 종료 기다림)
    setTimeout(() => {
      console.log('Master process exiting.');
      process.exit(0);
    }, 5000);
  };

  process.on('SIGINT', shutdownMaster);
  process.on('SIGTERM', shutdownMaster);

  process.on('uncaughtException', (err) => {
    console.error('Uncaught Exception in master:', err);
    shutdownMaster();
  });

  process.on('unhandledRejection', (reason) => {
    console.error('Unhandled Rejection in master:', reason);
    shutdownMaster();
  });

  const { subscriber } = require('./infra/db/subscriberRedisClient');

  subscriber.connect();

  let debounceTimer = null;
  let updatePending = false;
  // SSE 데이터 전송 디바운싱

  subscriber.subscribe('location_updates', () => {
    updatePending = true;
    
    if (debounceTimer) return;

    debounceTimer = setTimeout(async function sendUpdate() {
      debounceTimer = null;

      if (!updatePending) return;

      try {
        const allLocations = await getAllRecentLocations(); // Redis 조회
        const payload = JSON.stringify({ type: 'location_update', data: allLocations });

        for (const id in cluster.workers) {
          cluster.workers[id].send(payload); // 워커들에게 데이터 전송
        }

        console.log(`[MASTER] Broadcasted location update to ${Object.keys(cluster.workers).length} workers`);
      } catch (err) {
        console.error('[MASTER] Failed to fetch/broadcast location updates:', err);
      }

      updatePending = false;
    }, 500); // 0.5초 주기
  });

  cluster.on('message', (worker, message) => {
    if (!message || message.type !== 'sensor_stream') {
      return;
    }

    for (const id in cluster.workers) {
      const targetWorker = cluster.workers[id];
      if (!targetWorker || targetWorker.id === worker.id) {
        continue;
      }
      targetWorker.send(message);
    }
  });

} else {
  const shutdownWorker = async (signal) => {
    console.log(`Worker ${process.pid} received ${signal}, shutting down...`);
    try {
      await redisPool.drain();
      await redisPool.clear();
    } catch (err) {
      console.error('Error during redis cleanup:', err);
    } finally {
      process.exit(0);
    }
  };

  process.on('SIGINT', () => shutdownWorker('SIGINT'));
  process.on('SIGTERM', () => shutdownWorker('SIGTERM'));

  process.on('uncaughtException', (err) => {
    console.error(`Uncaught Exception in worker ${process.pid}:`, err);
    shutdownWorker('uncaughtException');
  });

  process.on('unhandledRejection', (reason) => {
    console.error(`Unhandled Rejection in worker ${process.pid}:`, reason);
    shutdownWorker('unhandledRejection');
  });

  process.on('message', (message) => {
    handleMasterBroadcast(message);
  });

  startServer();
}
