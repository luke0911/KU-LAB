const express = require('express');
const router = express.Router();
const locationService = require('../application/locationService.js');
const jwtAuth = require('../infra/auth/jwtProcessor')
const { success, error } = require('../global/response/commonResponse');

router.use(jwtAuth);

/**
 * @swagger
 * tags:
 *   name: Location
 *   description: 사용자 위치 정보 API
 */

/**
 * @swagger
 * /locations:
 *   post:
 *     summary: 사용자 위치 저장
 *     tags: [Location]
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
 *               userX:
 *                 type: number
 *                 example: 300.0
 *               userY:
 *                 type: number
 *                 example: 200.0
 *               userZ:
 *                 type: number
 *                 example: 10.3
 *               userDirection:
 *                 type: number
 *                 example: 90.0
 *               userFloor:
 *                 type: number
 *                 example: 1.5
 *               userStatus:
 *                 type: string
 *                 example: "Active"
 *               background:
 *                 type: boolean
 *                 example: false
 *     responses:
 *       201:
 *         description: 저장된 위치 정보 반환
 *       400:
 *         description: 잘못된 요청
 */
router.post('/', async (req, res) => {
  try {
    const payload = {
      ...req.body,
      userId: req.userDetails.userId,
      role: req.userDetails.role
    };
    const authHeader = req.headers.authorization;
    const result = await locationService.saveLocation(payload, authHeader);
    res.status(201).json(success(201, result));
  } catch (err) {
    res.status(400).json(error(400, err.message));
  }
});

/**
 * @swagger
 * /locations/latest:
 *   get:
 *     summary: 특정 사용자의 최신 위치 조회
 *     tags: [Location]
 *     responses:
 *       200:
 *         description: 사용자 최근 위치 정보
 *       400:
 *         description: 요청 실패
 *       404:
 *         description: 해당 사용자의 위치 정보가 없음
 */
router.get('/latest', async (req, res) => {
  const userId = req.userDetails.userId;

  try {
    const result = await locationService.getLatestLocationbyUserId(userId);

    if (!result) {
      return res.status(404).json(error(404, 'Location data not found for the specified user'));
    }

    res.status(200).json(success(200, result));
  } catch (err) {
    res.status(400).json(error(400, err.message));
  }
});

/**
 * @swagger
 * /locations/recent-all:
 *   get:
 *     summary: 전체 사용자의 최신 위치 조회
 *     tags: [Location]
 *     responses:
 *       200:
 *         description: 전체 사용자 최근 위치 정보
 *       400:
 *         description: 요청 실패
 */
router.get('/recent-all', async (req, res) => {
  try {
    const result = await locationService.getAllRecentLocations();
    res.status(200).json(success(200, result));
  } catch (err) {
    res.status(400).json(error(400, err.message));
  }
});

/**
 * @swagger
 * /locations/{userId}:
 *   get:
 *     summary: 특정 사용자의 위치 로그 전체 조회
 *     tags: [Location]
 *     parameters:
 *       - in: path
 *         name: userId
 *         required: true
 *         schema:
 *           type: string
 *           example: "01971fc6-543e-7955-9619-5b90da1384b5"
 *         description: 사용자 UUID
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
 *         description: 사용자 위치 기록
 *       400:
 *         description: 요청 실패
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
    const result = await locationService.getUserLocationLogs(userId, page, limit);
    res.status(200).json(success(200, result));
  } catch (err) {
    res.status(400).json(error(400, err.message));
  }
});

/**
 * @swagger
 * /locations/{userId}/period:
 *   get:
 *     summary: 특정 사용자의 위치 로그를 기간별로 조회
 *     tags: [Location]
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
 *         description: 선택한 기간 내 위치 로그 반환
 *       400:
 *         description: 잘못된 요청 또는 파라미터 누락
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
    const result = await locationService.getLocationLogsByPeriod(userId, from, to, page, limit);
    res.status(200).json(success(200, result));
  } catch (err) {
    res.status(400).json(error(400, err.message));
  }
});

module.exports = router;
