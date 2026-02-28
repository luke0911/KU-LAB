const express = require("express");
const router = express.Router();
const sensorService = require("../application/sensorService");
const button1Service = require("../application/button1Service");
const button2Service = require("../application/button2Service");
const button3Service = require("../application/button3Service");
const sensorStreamService = require("../application/sensorStreamService");
const jwtAuth = require("../infra/auth/jwtProcessor");
const { success, error } = require("../global/response/commonResponse");

router.use(jwtAuth);

// ========================================
// 기존 /sensors API (공통 센서데이터 저장)
// ========================================
/**
 * @swagger
 * tags:
 *   name: Sensor
 *   description: 사용자 센서 데이터 API
 */

/**
 * @swagger
 * /sensors:
 *   post:
 *     summary: 센서 데이터 저장
 *     tags: [Sensor]
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             properties:
 *               mapId:
 *                 type: integer
 *                 example: 1
 *               acceleration:
 *                 type: object
 *                 properties:
 *                   x:
 *                     type: number
 *                     example: 0.12
 *                   y:
 *                     type: number
 *                     example: -0.98
 *                   z:
 *                     type: number
 *                     example: 9.8
 *               magnetic:
 *                 type: object
 *                 properties:
 *                   x:
 *                     type: number
 *                     example: 30.5
 *                   y:
 *                     type: number
 *                     example: 25.3
 *                   z:
 *                     type: number
 *                     example: 35.2
 *               gyro:
 *                 type: object
 *                 properties:
 *                   x:
 *                     type: number
 *                     example: 0.01
 *                   y:
 *                     type: number
 *                     example: 0.02
 *                   z:
 *                     type: number
 *                     example: 0.03
 *               linearAcceleration:
 *                 type: object
 *                 properties:
 *                   x:
 *                     type: number
 *                     example: 0.5
 *                   y:
 *                     type: number
 *                     example: 0.7
 *                   z:
 *                     type: number
 *                     example: 1.2
 *               rotation:
 *                 type: object
 *                 properties:
 *                   x:
 *                     type: number
 *                     example: 0.12
 *                   y:
 *                     type: number
 *                     example: 0.45
 *                   z:
 *                     type: number
 *                     example: 0.98
 *               pressure:
 *                 type: number
 *                 example: 1012.5
 *               light:
 *                 type: number
 *                 example: 120.5
 *               proximity:
 *                 type: number
 *                 example: 1.3
 *               rf:
 *                 type: number
 *                 example: 12.3
 *               userStateReal:
 *                 type: number
 *                 example: 0
 *     responses:
 *       201:
 *         description: 저장된 센서 데이터 반환
 *       400:
 *         description: 잘못된 요청
 */
router.post('/', async (req, res) => {
  try {
    const payload = {
      ...req.body,
      userId: req.userDetails.userId
    };
    const result = await sensorService.saveSensorData(payload);
    res.status(201).json(success(201, result));
  } catch (err) {
    res.status(400).json(error(400, err.message));
  }
});

/**
 * @swagger
 * /sensors/latest/{userId}:
 *   get:
 *     summary: 특정 사용자의 최신 센서 데이터 조회
 *     tags: [Sensor]
 *     parameters:
 *       - in: path
 *         name: userId
 *         required: true
 *         schema:
 *           type: string
 *           example: "01971fc6-543e-7955-9619-5b90da1384b5"
 *         description: 사용자 UUID
 *     responses:
 *       200:
 *         description: 최신 센서 데이터 1건 반환
 *       404:
 *         description: 해당 사용자의 센서 데이터가 없음
 *       400:
 *         description: 잘못된 요청
 */
router.get('/latest/:userId', async (req, res) => {
  const { userId } = req.params;

  try {
    const result = await sensorService.getLatestSensorLogByUserId(userId);

    if (!result) {
      return res.status(404).json(error(404, 'Sensor data not found for the specified user'));
    }

    res.status(200).json(success(200, result));
  } catch (err) {
    res.status(400).json(error(400, err.message));
  }
});

/**
 * @swagger
 * /sensors/recent/{userId}:
 *   get:
 *     summary: 사용자의 최신 센서 데이터 조회
 *     tags: [Sensor]
 *     parameters:
 *       - in: path
 *         name: userId
 *         required: true
 *         schema:
 *           type: string
 *           example: "01971fc6-543e-7955-9619-5b90da1384b5"
 *         description: 사용자 UUID
 *     responses:
 *       200:
 *         description: 사용자 최신 센서 정보
 *       400:
 *         description: 요청 실패
 */
router.get('/recent/:userId', async (req, res) => {
  const { userId } = req.params;

  try {
    const result = await sensorService.getRecentSensorLog(userId);
    res.status(200).json(success(200, result));
  } catch (err) {
    res.status(400).json(error(400, err.message));
  }
});

/**
 * @swagger
 * /sensors/stream:
 *   get:
 *     summary: 활성화된 실시간 센서 퍼블리셔 목록 조회
 *     tags: [Sensor]
 *     responses:
 *       200:
 *         description: 실시간 데이터 발행 중인 디바이스 목록
 */
router.get('/stream', async (req, res) => {
  try {
    const devices = await sensorStreamService.listActiveDevices();
    res.status(200).json(success(200, { devices }, 'Active realtime devices'));
  } catch (err) {
    console.error('[GET /sensors/stream] error:', err);
    res.status(500).json(error(500, 'Failed to load realtime device list'));
  }
});

/**
 * @swagger
 * /sensors/stream/{deviceId}:
 *   get:
 *     summary: 특정 디바이스의 최신 실시간 센서 샘플 조회
 *     tags: [Sensor]
 *     parameters:
 *       - in: path
 *         name: deviceId
 *         required: true
 *         schema:
 *           type: string
 *         description: 디바이스 ID (Android 단말 UUID 등)
 *     responses:
 *       200:
 *         description: 최신 실시간 센서 샘플
 *       404:
 *         description: 해당 디바이스에 대한 실시간 데이터가 없음
 */
router.get('/stream/:deviceId', async (req, res) => {
  const { deviceId } = req.params;

  try {
    const sample = await sensorStreamService.getLatestSample(deviceId);
    if (!sample) {
      return res.status(404).json(error(404, 'No realtime sensor sample found for the specified device'));
    }
    res.status(200).json(success(200, sample, 'Latest realtime sensor sample'));
  } catch (err) {
    console.error(`[GET /sensors/stream/${deviceId}] error:`, err);
    res.status(500).json(error(500, 'Failed to load realtime sensor sample'));
  }
});

/**
 * @swagger
 * /sensors/{userId}:
 *   get:
 *     summary: 특정 사용자의 모든 센서 데이터 조회
 *     tags: [Sensor]
 *     parameters:
 *       - in: path
 *         name: userId
 *         required: true
 *         description: 센서 데이터를 조회할 사용자 ID
 *         schema:
 *           type: string
 *           example: "01971fc6-543e-7955-9619-5b90da1384b5"
 *       - in: query
 *         name: page
 *         schema:
 *           type: number
 *           default: 1
 *         description: 페이지 번호
 *       - in: query
 *         name: limit
 *         schema:
 *           type: number
 *           default: 20
 *         description: 페이지당 항목 수
 *     responses:
 *       200:
 *         description: 해당 사용자의 센서 데이터 전체 반환
 *       400:
 *         description: 잘못된 요청
 */
router.get('/:userId', async (req, res) => {
  const { userId } = req.params;
  let page = parseInt(req.query.page);
  let limit = parseInt(req.query.limit);

  if (req.query.page === undefined || req.query.page === null || req.query.page === '') page = 1;
  if (req.query.limit === undefined || req.query.limit === null || req.query.limit === '') limit = 20;

  if (isNaN(page) || page < 1) {
    return res.status(400).json(error(400, 'Query parameter "page" must be greater than or equal to 1'));
  }

  if (isNaN(limit) || limit < 1) {
    return res.status(400).json(error(400, 'Query parameter "limit" must be greater than or equal to 1'));
  }
  
  try {
    const result = await sensorService.getAllUserSensorLogs(userId, page, limit);
    res.status(200).json(success(200, result));
  } catch (err) {
    res.status(400).json(error(400, err.message));
  }
});

/**
 * @swagger
 * /sensors/{userId}/period:
 *   get:
 *     summary: 특정 사용자의 센서 데이터 기간 조회
 *     tags: [Sensor]
 *     parameters:
 *       - in: path
 *         name: userId
 *         required: true
 *         schema:
 *           type: string
 *           example: "01971fc6-543e-7955-9619-5b90da1384b5"
 *         description: 사용자 UUID
 *       - in: query
 *         name: from
 *         required: true
 *         schema:
 *           type: string
 *           format: date-time
 *           example: "2025-05-01T00:00:00"
 *         description: 시작 시각 (ISO 형식) 2025-05-01T00:00:00
 *       - in: query
 *         name: to
 *         required: true
 *         schema:
 *           type: string
 *           format: date-time
 *           example: "2025-05-20T23:59:59"
 *         description: 종료 시각 (ISO 형식) 2025-05-20T23:59:59
 *       - in: query
 *         name: page
 *         schema:
 *           type: number
 *           default: 1
 *         description: 페이지 번호
 *       - in: query
 *         name: limit
 *         schema:
 *           type: number
 *           default: 20
 *         description: 페이지당 항목 수
 *     responses:
 *       200:
 *         description: 사용자 센서 데이터 로그
 *       400:
 *         description: 요청 실패
 */
router.get('/:userId/period', async (req, res) => {
  const { userId } = req.params;
  const { from, to } = req.query;
  let page = parseInt(req.query.page);
  let limit = parseInt(req.query.limit);

  if (req.query.page === undefined || req.query.page === null || req.query.page === '') page = 1;
  if (req.query.limit === undefined || req.query.limit === null || req.query.limit === '') limit = 20;

  if (!from || !to) {
    return res.status(400).json(error(400, 'Query parameters "from" and "to" are required'));
  }

  if (isNaN(page) || page < 1) {
    return res.status(400).json(error(400, 'Query parameter "page" must be greater than or equal to 1'));
  }

  if (isNaN(limit) || limit < 1) {
    return res.status(400).json(error(400, 'Query parameter "limit" must be greater than or equal to 1'));
  }

  try {
    const result = await sensorService.getSensorLogsByPeriod(userId, from, to, page, limit);
    res.status(200).json(success(200, result));
  } catch (err) {
    res.status(400).json(error(400, err.message));
  }
});

// ========================================
// 버튼1 전용 API (Step)
// ========================================
router.post("/button1", async (req, res) => {
  try {
    const payload = {
      ...req.body,
      userId: req.userDetails.userId,
    };
    const result = await button1Service.saveButton1Data(payload.userId, payload);
    res.status(201).json(success(201, result));
  } catch (err) {
    res.status(400).json(error(400, err.message));
  }
});

router.get("/button1/latest/:userId", async (req, res) => {
  try {
    const result = await button1Service.getLatestButton1Data(req.params.userId);
    res.status(200).json(success(200, result));
  } catch (err) {
    res.status(400).json(error(400, err.message));
  }
});

// ========================================
// 버튼2 전용 API (Pressure + Gyro + Walking + Floor)
// ========================================
router.post("/button2", async (req, res) => {
  try {
    const payload = {
      ...req.body,
      userId: req.userDetails.userId,
    };
    const result = await button2Service.saveButton2Data(payload.userId, payload);
    res.status(201).json(success(201, result));
  } catch (err) {
    res.status(400).json(error(400, err.message));
  }
});

router.get("/button2/latest/:userId", async (req, res) => {
  try {
    const result = await button2Service.getLatestButton2Data(req.params.userId);
    res.status(200).json(success(200, result));
  } catch (err) {
    res.status(400).json(error(400, err.message));
  }
});

// ========================================
// 버튼3 전용 API (Accel + Walking 여부)
// ========================================
router.post("/button3", async (req, res) => {
  try {
    const payload = {
      ...req.body,
      userId: req.userDetails.userId,
    };
    const result = await button3Service.saveButton3Data(payload.userId, payload);
    res.status(201).json(success(201, result));
  } catch (err) {
    res.status(400).json(error(400, err.message));
  }
});

router.get("/button3/latest/:userId", async (req, res) => {
  try {
    const result = await button3Service.getLatestButton3Data(req.params.userId);
    res.status(200).json(success(200, result));
  } catch (err) {
    res.status(400).json(error(400, err.message));
  }
});

module.exports = router;
