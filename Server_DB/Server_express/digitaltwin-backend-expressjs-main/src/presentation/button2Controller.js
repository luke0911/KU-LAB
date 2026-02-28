const express = require("express");
const router = express.Router();
const button2Service = require("../application/button2Service");
const jwtAuth = require("../infra/auth/jwtProcessor");
const { success, error } = require("../global/response/commonResponse");

router.use(jwtAuth);

/**
 * @swagger
 * tags:
 *   name: Button2
 *   description: Button2 센서 데이터 API
 */

/**
 * @swagger
 * /button2:
 *   post:
 *     summary: Button2 센서 데이터 저장
 *     tags: [Button2]
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             properties:
 *               pressure:
 *                 type: number
 *                 example: 1013.2
 *               gyro:
 *                 type: object
 *                 properties:
 *                   x:
 *                     type: number
 *                     example: 0.02
 *                   y:
 *                     type: number
 *                     example: 0.03
 *                   z:
 *                     type: number
 *                     example: 0.04
 *               isWalking:
 *                 type: integer
 *                 example: 1   # 1=걷는중, 0=정지
 *               floor:
 *                 type: integer
 *                 example: 3
 *     responses:
 *       201:
 *         description: 저장된 Button2 데이터
 */
router.post("/", async (req, res) => {
  try {
    const payload = { ...req.body };
    const result = await button2Service.saveButton2Data(req.userDetails.userId, payload);
    res.status(201).json(success(201, result));
  } catch (err) {
    res.status(400).json(error(400, err.message));
  }
});

/**
 * @swagger
 * /button2/latest/{userId}:
 *   get:
 *     summary: 특정 사용자의 최신 Button2 데이터 조회
 *     tags: [Button2]
 *     parameters:
 *       - in: path
 *         name: userId
 *         required: true
 *         schema:
 *           type: string
 *           example: "01971fc6-543e-7955-9619-5b90da1384b5"
 *     responses:
 *       200:
 *         description: 최신 Button2 데이터
 */
router.get("/latest/:userId", async (req, res) => {
  try {
    const result = await button2Service.getLatestButton2Data(req.params.userId);
    res.status(200).json(success(200, result));
  } catch (err) {
    res.status(400).json(error(400, err.message));
  }
});

module.exports = router;
