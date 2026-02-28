const express = require("express");
const router = express.Router();
const button1Service = require("../application/button1Service");
const jwtAuth = require("../infra/auth/jwtProcessor");
const { success, error } = require("../global/response/commonResponse");

router.use(jwtAuth);

/**
 * @swagger
 * tags:
 *   name: Button1
 *   description: Button1 센서 데이터 API
 */

/**
 * @swagger
 * /button1:
 *   post:
 *     summary: Button1 센서 데이터 저장
 *     tags: [Button1]
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             properties:
 *               gyroAngle:
 *                 type: number
 *                 example: 45.3
 *               magneticVector:
 *                 type: object
 *                 properties:
 *                   x: { type: number, example: 30.5 }
 *                   y: { type: number, example: 25.3 }
 *                   z: { type: number, example: 35.2 }
 *               stepLength:
 *                 type: number
 *                 example: 0.75
 *               gripState:
 *                 type: integer
 *                 example: 1   # 1=잡음, 0=안잡음
 *     responses:
 *       201:
 *         description: 저장된 Button1 데이터
 */
router.post("/", async (req, res) => {
  try {
    const payload = { ...req.body };
    const result = await button1Service.saveButton1Data(req.userDetails.userId, payload);
    res.status(201).json(success(201, result));
  } catch (err) {
    res.status(400).json(error(400, err.message));
  }
});

/**
 * @swagger
 * /button1/latest/{userId}:
 *   get:
 *     summary: 특정 사용자의 최신 Button1 데이터 조회
 *     tags: [Button1]
 *     parameters:
 *       - in: path
 *         name: userId
 *         required: true
 *         schema:
 *           type: string
 *           example: "01971fc6-543e-7955-9619-5b90da1384b5"
 *     responses:
 *       200:
 *         description: 최신 Button1 데이터
 */
router.get("/latest/:userId", async (req, res) => {
  try {
    const result = await button1Service.getLatestButton1Data(req.params.userId);
    res.status(200).json(success(200, result));
  } catch (err) {
    res.status(400).json(error(400, err.message));
  }
});

module.exports = router;
