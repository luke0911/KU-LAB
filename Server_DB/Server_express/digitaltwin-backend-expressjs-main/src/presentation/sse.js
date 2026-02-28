const express = require('express');
const router = express.Router();
const { getAllRecentLocations } = require('../infra/db/repository/locationAdapter');
const jwt = require('jsonwebtoken'); // SSE는 JWT 토큰 검증 따로
const { jwtSecretKey } = require('../config/env');
const { success, error } = require('../global/response/commonResponse');

const clients = new Set();

setInterval(() => {
  if (clients.size === 0) return; // 연결된 클라이언트가 없으면 종료

  for (const res of clients) {
    res.write(`: keep-alive\n\n`); // SSE keep-alive 요청
  }
  console.log(`[SSE] Sent keep-alive to ${clients.size} clients`);
}, 30000); // 30초 주기

process.on('message', (message) => {
  try {
    const { type, data } = JSON.parse(message);

    if (type === 'location_update') {
      for (const res of clients) {
        res.write(`data: ${JSON.stringify({ message: '새로운 위치 정보 업데이트', data: data })}\n\n`);
      }
      if(clients.size !== 0)
        console.log(`[WORKER ${process.pid}] Sent location update to ${clients.size} clients`);
    }
  } catch (err) {
    console.error('[WORKER] Failed to handle incoming message:', err);
  }
});

router.get('/locations/recent', async (req, res) => {
  
  const token = req.query.jwt; // jwt 토큰 쿼리파라미터
  if (!token) {
    res.status(401).setHeader('Content-Type', 'application/json');
    res.end(JSON.stringify(error(401, 'JWT 토큰이 필요합니다. /locations/recent?jwt=... 으로 전달하세요.')));
    return;
  }

  let claims;
  try {
    claims = jwt.verify(token, jwtSecretKey);
    console.log(`[SSE] Client connected (total: ${clients.size}) userId: ${claims.sub}, role: ${claims.role}`);
  } catch (err) {
    if (err.name === 'TokenExpiredError') {
      res.status(401).setHeader('Content-Type', 'application/json');
      res.end(JSON.stringify(error(401, 'AccessToken이 만료되었습니다. RefreshToken으로 토큰을 재발급 받으세요.')));
      return;
    }
    res.status(401).setHeader('Content-Type', 'application/json');
    res.end(JSON.stringify(error(401, '유효하지 않은 AccessToken입니다.')));
    return;
  }

  res.setHeader('Content-Type', 'text/event-stream');
  res.setHeader('Cache-Control', 'no-cache');
  res.setHeader('Connection', 'keep-alive');
  res.flushHeaders();

  // ✅ 최초 연결시 최소 1번 응답을 보내야 브라우저가 에러로 간주하지 않음
  const allLocations = await getAllRecentLocations(); // Redis 조회
  res.write(`data: ${JSON.stringify({ message: 'SSE 연결 성공 및 현재 최신 위치 리스트 조회', data: allLocations })}\n\n`);

  clients.add(res);
  console.log(`[SSE] Client connected (total: ${clients.size})`);

  req.on('close', () => {
    clients.delete(res);
    console.log(`[SSE] Client disconnected (remaining: ${clients.size})`);
  });
});

module.exports = router;