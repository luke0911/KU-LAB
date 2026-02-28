// app.js
const express = require("express");
const bodyParser = require("body-parser");
const locationRouter = require("./presentation/locationController");
const sensorRouter = require("./presentation/sensorController");
const poiStayRouter = require("./presentation/poiStayController");
const sseRouter = require("./presentation/sse");
const workerInfoRouter = require('./presentation/workerInfoController'); // 1013 김명권 추가
require("./config/env"); // 환경 설정 파일 로드
const swaggerUi = require("swagger-ui-express");
const swaggerSpec = require("./config/swaggerConfig");
const cors = require("cors");
const corsOptions = require("./config/corsConfig");
const { success, error } = require("./global/response/commonResponse");

const app = express();


//김준하 추가
// 시각화 데이터 API
const sensorRepository = require("./domain/repository/sensorRepository");
//김준하 추가
const path = require("path"); // 맨 위 require 부분에 추가
const button1Router = require("./presentation/button1Controller");
const button2Router = require("./presentation/button2Controller");
const button3Router = require("./presentation/button3Controller");

//김준하 추가
// visualization/start 라우트 추가
app.post("/visualization/start", (req, res) => {
  console.log("시각화 시작 요청 받음"); // 버튼 클릭하면 여기 로그가 찍힘
  res.send({ message: "시각화 시작됨" });
});



//김준하 추가
app.get("/api/visualization-data", async (req, res) => {
  try {
    const { userId } = req.query; // /api/visualization-data?userId=xxx
    if (!userId) {
      return res.status(400).json({ error: "userId is required" });
    }

    const log = await sensorRepository.getRecentSensorLog(userId);

    if (!log) {
      return res.status(404).json({ error: "No sensor data found" });
    }

    // 시각화용 데이터 변환
    const labels = ["자이로각도", "자기장벡터", "보폭", "파지상태"];
    const values = [
      log.rotation?.x ?? 0,
      log.magnetic?.x ?? 0,
      log.stepLength ?? 0,
      log.userStateReal ?? 0,
    ];

    res.json({ labels, values });
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: "데이터 로드 실패" });
  }
});

//김준하 추가
app.get("/api/visualization-stream", async (req, res) => {
  const { userId } = req.query;
  if (!userId) {
    return res.status(400).json({ error: "userId is required" });
  }

  res.setHeader("Content-Type", "text/event-stream");
  res.setHeader("Cache-Control", "no-cache");
  res.setHeader("Connection", "keep-alive");

  const interval = setInterval(async () => {
    const log = await sensorRepository.getRecentSensorLog(userId);
    if (log) {
      const labels = ["자이로각도", "자기장벡터", "보폭", "파지상태"];
      const values = [
        log.rotation?.x ?? 0,
        log.magnetic?.x ?? 0,
        log.stepLength ?? 0,
        log.userStateReal ?? 0,
      ];

      res.write(`data: ${JSON.stringify({ labels, values })}\n\n`);
    }
  }, 1000); // 1초마다 최신 값 전송

  req.on("close", () => {
    clearInterval(interval);
  });
});

//김준하 추가
app.use(express.static(path.join(__dirname, "public"))); // public 폴더 서빙

app.use(bodyParser.json());

app.use((err, req, res, next) => {
  // body-parser에서 발생하는 JSON 파싱 에러는 SyntaxError입니다.
  if (err instanceof SyntaxError && err.status === 400 && "body" in err) {
    console.error(`[ERROR] JSON Parse Error on ${req.method} ${req.originalUrl}`);
    console.error(`Message: ${err.message}`);
    console.error(err.stack);
    return res.status(400).json(error(400, "Invalid JSON format in request body"));
  }
  // 다른 종류의 에러는 다음 에러 미들웨어로 전달
  next(err);
});

// 요청 URL + 응답시간 + 일반 로깅 미들웨어
app.use((req, res, next) => {
  const start = Date.now();
  res.on("finish", () => {
    const duration = Date.now() - start;
    console.log(`[${res.statusCode}] ${req.method} ${req.originalUrl} - ${duration}ms`);
  });
  next();
});

app.get("/express", (req, res) => {
  res.send("Hello World");
});

app.use("/express/api-docs", swaggerUi.serve, swaggerUi.setup(swaggerSpec));
app.use(cors(corsOptions));
app.use("/express/locations", locationRouter);
app.use("/express/sse", sseRouter);
app.use("/express/sensors", sensorRouter);
app.use("/express/poi-stay", poiStayRouter);
app.use('/express/worker-info', workerInfoRouter); // ✨ 1013 김명권 추가

//0921 김준하 추가
app.use("/express/button1", button1Router);
app.use("/express/button2", button2Router);
app.use("/express/button3", button3Router);

// 이 미들웨어는 위에서 처리되지 않은 모든 종류의 에러를 처리합니다.
app.use((err, req, res, next) => {
  console.error(`[FINAL ERROR HANDLER] ${req.method} ${req.originalUrl}`);
  console.error(`Message: ${err.message}`);
  console.error(err.stack);

  // 이미 응답 헤더가 전송되었는지 다시 확인
  if (res.headersSent) {
    return next(err);
  }

  // 기본 500 에러 응답
  res.status(500).json(error(500, "Internal Server Error"));
});


//김준하 추가
app.use("/express/csvs", express.static(path.join(__dirname, "public", "csvs")));

// CSV 라우터 마운트
const csvController = require("./presentation/csvController");
app.use("/express", csvController);

module.exports = app; // 서버 시작 기능 제거하고 앱 객체만 export
