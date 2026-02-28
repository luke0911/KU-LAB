const jwt = require('jsonwebtoken');
const { jwtSecretKey } = require('../../config/env');

function jwtAuth(req, res, next) {
    const authHeader = req.headers.authorization;
    if (!authHeader || !authHeader.startsWith('Bearer ')) {
        return res.status(401).json({ message: 'Authorization 헤더가 필요합니다. (예: Bearer {token})' });
    }

    const token = authHeader.substring(7);

    try {
        // 검증 및 payload 추출
        const claims = jwt.verify(token, jwtSecretKey);
        req.userDetails = {
            userId: claims.sub,  // subject (userId)
            role: claims.role    // role claim
        };
        next();
    } catch (err) {
        if (err.name === 'TokenExpiredError') {
            return res.status(401).json({ message: 'AccessToken이 만료되었습니다. RefreshToken으로 토큰을 재발급 받으세요.' });
        }
        return res.status(401).json({ message: '유효하지 않은 AccessToken입니다.' });
    }
}

module.exports = jwtAuth;