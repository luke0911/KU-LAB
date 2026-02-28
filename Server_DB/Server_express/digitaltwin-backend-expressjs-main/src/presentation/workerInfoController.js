// src/presentation/workerInfoController.js

const express = require('express');
const router = express.Router();
const workerInfoService = require('../application/workerInfoService.js');
const jwtAuth = require('../infra/auth/jwtProcessor');
const { success, error } = require('../global/response/commonResponse');

router.use(jwtAuth);

/**
 * @swagger
 * tags:
 *   name: WorkerInfo
 *   description: 작업자 정보 관리 API
 */

/**
 * @swagger
 * /worker-info:
 *   post:
 *     summary: 작업자 정보 생성 (관리자는 ID 지정 가능)
 *     tags: [WorkerInfo]
 *     description: |
 *       - 일반 사용자는 자신의 정보만 생성할 수 있습니다 (JWT 토큰 기준).
 *       - 'admin' 역할을 가진 사용자는 request body에 `userId`를 포함하여 특정 작업자의 정보를 생성할 수 있습니다.
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             properties:
 *               userId:
 *                 type: string
 *                 description: "(관리자용) 정보를 생성할 작업자의 UUID"
 *                 example: "01971fc6-543e-7955-9619-5b90da1384b5"
 *               entryTime:
 *                 type: string
 *                 format: date-time
 *                 example: "2025-10-13T09:00:00Z"
 *               workDetails:
 *                 type: string
 *                 example: "서버실 일일 점검"
 *               position:
 *                 type: string
 *                 example: "시스템 엔지니어"
 *     responses:
 *       201:
 *         description: "작업자 정보 생성 성공"
 *       400:
 *         description: "잘못된 요청 (관리자가 userId를 누락한 경우)"
 *       403:
 *         description: "권한 없음"
 */
router.post('/', async (req, res) => {
  try {
    let targetUserId;

    // 역할(role)에 따른 userId 분기 처리
    if (req.userDetails.role === 'admin' || req.userDetails.role === 'MASTER') {
      // 관리자인 경우: request body에서 userId를 반드시 받아야 함
      if (!req.body.userId) {
        return res.status(400).json(error(400, "Admin must provide a 'userId' in the request body."));
      }
      targetUserId = req.body.userId;
    } else {
      // 일반 사용자인 경우: 자신의 JWT 토큰에 있는 userId를 사용
      targetUserId = req.userDetails.userId;
    }

    const payload = { ...req.body, userId: targetUserId };
    const result = await workerInfoService.createOrUpdateWorkerInfo(payload);
    res.status(201).json(success(201, result));
  } catch (err) {
    res.status(400).json(error(400, err.message));
  }
});

/**
 * @swagger
 * /worker-info/{userId}:
 *   get:
 *     summary: 특정 작업자 정보 조회
 *     tags: [WorkerInfo]
 *     parameters:
 *       - in: path
 *         name: userId
 *         required: true
 *         schema:
 *           type: string
 *         description: 조회할 작업자의 UUID
 *         example: "01971fc6-543e-7955-9619-5b90da1384b5"
 *     responses:
 *       200:
 *         description: "조회 성공"
 *       404:
 *         description: "해당 작업자 정보를 찾을 수 없음"
 */
router.get('/:userId', async (req, res) => {
  try {
    const { userId } = req.params;
    const result = await workerInfoService.getWorkerInfo(userId);
    if (!result) {
      return res.status(404).json(error(404, 'Worker info not found'));
    }
    res.status(200).json(success(200, result));
  } catch (err) {
    res.status(400).json(error(400, err.message));
  }
});

/**
 * @swagger
 * /worker-info/{userId}:
 *   put:
 *     summary: 특정 작업자 정보 수정 (부분 업데이트)
 *     tags: [WorkerInfo]
 *     parameters:
 *       - in: path
 *         name: userId
 *         required: true
 *         schema:
 *           type: string
 *         description: 수정할 작업자의 UUID
 *         example: "01971fc6-543e-7955-9619-5b90da1384b5"
 *     requestBody:
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             properties:
 *               exitTime:
 *                 type: string
 *                 format: date-time
 *                 example: "2025-10-13T18:00:00Z"
 *               workDetails:
 *                 type: string
 *                 example: "서버 점검 완료 및 보고서 작성"
 *     responses:
 *       200:
 *         description: "수정 성공"
 *       404:
 *         description: "수정할 작업자 정보를 찾을 수 없음"
 */
router.put('/:userId', async (req, res) => {
    try {
        const { userId } = req.params;
        const result = await workerInfoService.updateWorkerInfo(userId, req.body);
        if (!result) {
            return res.status(404).json(error(404, 'Worker info not found to update'));
        }
        res.status(200).json(success(200, result, "Successfully updated."));
    } catch (err) {
        res.status(400).json(error(400, err.message));
    }
});

/**
 * @swagger
 * /worker-info/{userId}:
 *   delete:
 *     summary: 특정 작업자 정보 삭제
 *     tags: [WorkerInfo]
 *     parameters:
 *       - in: path
 *         name: userId
 *         required: true
 *         schema:
 *           type: string
 *         description: 삭제할 작업자의 UUID
 *         example: "01971fc6-543e-7955-9619-5b90da1384b5"
 *     responses:
 *       200:
 *         description: "삭제 성공"
 */
router.delete('/:userId', async (req, res) => {
    try {
        const { userId } = req.params;
        await workerInfoService.deleteWorkerInfo(userId);
        res.status(200).json(success(200, null, "Successfully deleted."));
    } catch (err) {
        res.status(400).json(error(400, err.message));
    }
});

module.exports = router;
