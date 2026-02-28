const express = require("express");
const fs = require("fs");
const path = require("path");
const csvParser = require("csv-parser"); // npm install csv-parser
const { upload } = require("../infra/upload/multer");
const service = require("../application/csvService");
const { success, error } = require("../global/response/commonResponse"); // ✅ 이름 맞춤

const router = express.Router();
const CSV_DIR = path.join(__dirname, "../public/csvs");

/**
 * @openapi
 * /csvs:
 *   post:
 *     summary: Upload a CSV file
 *     description: CSV 파일을 업로드하여 서버의 /express/csvs 폴더에 저장하고, 메타데이터를 DB에 기록합니다.
 *     tags:
 *       - CSV
 *     security:
 *       - JWT-Token: []
 *     requestBody:
 *       required: true
 *       content:
 *         multipart/form-data:
 *           schema:
 *             type: object
 *             properties:
 *               file:
 *                 type: string
 *                 format: binary
 *               uploader:
 *                 type: string
 *                 example: android_app_user
 *     responses:
 *       200:
 *         description: 업로드 성공
 *       400:
 *         description: 잘못된 요청 (파일 없음)
 *       500:
 *         description: 서버 오류
 */
router.post("/csvs", upload.single("file"), async (req, res) => {
  try {
    if (!req.file) {
      return res.status(400).json(error(400, "CSV file is required"));
    }

    const publicUrl = `/express/csvs/${req.file.filename}`;
    const uploader = req.body.uploader || req.user?.sub || "unknown";

    await service.recordUpload({
      filename: req.file.originalname,
      path: req.file.path,
      url: publicUrl,
      size: req.file.size,
      mimetype: req.file.mimetype,
      uploader,
      extra: { body: req.body },
    });

    return res.json(
      success(200, {
        message: "Uploaded",
        file: {
          name: req.file.originalname,
          savedAs: req.file.filename,
          bytes: req.file.size,
          url: publicUrl,
        },
      })
    );
  } catch (e) {
    console.error("[POST /express/csvs] error:", e);
    return res.status(500).json(error(500, "Upload failed"));
  }
});

/**
 * @openapi
 * /csvs:
 *   get:
 *     summary: 업로드된 CSV 파일 목록 조회
 *     tags: [CSV]
 *     responses:
 *       200:
 *         description: CSV 파일 이름 리스트
 */
router.get("/csvs", async (req, res) => {
  try {
    const files = fs.readdirSync(CSV_DIR).filter((f) => f.endsWith(".csv"));
    return res.json(success(200, files, "목록 조회 성공"));
  } catch (e) {
    console.error("[GET /express/csvs] error:", e);
    return res.status(500).json(error(500, "목록 조회 실패"));
  }
});

/**
 * @openapi
 * /csvs/{filename}:
 *   get:
 *     summary: 특정 CSV 파일 내용 조회 (JSON 변환)
 *     tags: [CSV]
 *     parameters:
 *       - in: path
 *         name: filename
 *         schema:
 *           type: string
 *         required: true
 *         description: CSV 파일 이름
 *     responses:
 *       200:
 *         description: CSV 데이터(JSON 배열)
 *       404:
 *         description: 파일 없음
 */
router.get("/csvs/:filename", async (req, res) => {
  try {
    const { filename } = req.params;
    const filePath = path.join(CSV_DIR, filename);

    if (!fs.existsSync(filePath)) {
      return res.status(404).json(error(404, "해당 CSV 파일 없음"));
    }

    const results = [];
    fs.createReadStream(filePath)
      .pipe(csvParser())
      .on("data", (row) => results.push(row))
      .on("end", () => {
        return res.json(success(200, results, "CSV 파싱 성공"));
      })
      .on("error", (err) => {
        console.error("[CSV Parse Error]", err);
        return res.status(500).json(error(500, "CSV 파싱 실패"));
      });
  } catch (e) {
    console.error("[GET /express/csvs/:filename] error:", e);
    return res.status(500).json(error(500, "CSV 읽기 실패"));
  }
});

/**
 * @openapi
 * /csvs/raw/{filename}:
 *   get:
 *     summary: CSV 원본 파일 다운로드
 *     tags: [CSV]
 *     parameters:
 *       - in: path
 *         name: filename
 *         schema:
 *           type: string
 *         required: true
 *         description: CSV 파일 이름
 *     responses:
 *       200:
 *         description: CSV 원본 파일 다운로드
 *       404:
 *         description: 파일 없음
 */
router.get("/csvs/raw/:filename", (req, res) => {
  const { filename } = req.params;
  const filePath = path.join(CSV_DIR, filename);

  if (!fs.existsSync(filePath)) {
    return res.status(404).json(error(404, "해당 CSV 파일 없음"));
  }

  // 원본 CSV 그대로 다운로드
  res.download(filePath, filename);
});

module.exports = router;
