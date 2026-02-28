const express = require("express");
const router = express.Router();
const button3Service = require("../application/button3Service");
const jwtAuth = require("../infra/auth/jwtProcessor");
const { success, error } = require("../global/response/commonResponse");

router.use(jwtAuth);

/**
 * @swagger
 * tags:
 *   name: Button3
 *   description: Button3 센서 데이터 API
 */

/**
 * @swagger
 * /button3:
 *   post:
 *     summary: Button3 센서 데이터 저장
 *     tags: [Button3]
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             properties:
 *               acceleration:
 *                 type: string
 *                 example: "0.1,0.2,9.8"
 *               isWalking:
 *                 type: boolean
 *                 example: false
 *     responses:
 *       201:
 *         description: 저장된 Button3 데이터
 */
router.post("/", async (req, res) => {
  try {
    const payload = { ...req.body };
    const result = await button3Service.saveButton3Data(req.userDetails.userId, payload);
    res.status(201).json(success(201, result));
  } catch (err) {
    res.status(400).json(error(400, err.message));
  }
});

/**
 * @swagger
 * /button3/latest/{userId}:
 *   get:
 *     summary: 특정 사용자의 최신 Button3 데이터 조회
 *     tags: [Button3]
 *     parameters:
 *       - in: path
 *         name: userId
 *         required: true
 *         schema:
 *           type: string
 *           example: "01971fc6-543e-7955-9619-5b90da1384b5"
 *     responses:
 *       200:
 *         description: 최신 Button3 데이터
 */
router.get("/latest/:userId", async (req, res) => {
  try {
    const result = await button3Service.getLatestButton3Data(req.params.userId);
    res.status(200).json(success(200, result));
  } catch (err) {
    res.status(400).json(error(400, err.message));
  }
});

module.exports = router;
