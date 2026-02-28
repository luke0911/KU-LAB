require('dotenv').config();

module.exports = {
    port: process.env.PORT,
    host: process.env.SERVER_HOST,
    basePath: process.env.BASE_PATH,
    jwtSecretKey: process.env.JWT_SECRET_KEY,
    internalApiKey: process.env.INTERNAL_API_KEY,
    redis: {
        host: process.env.REDIS_HOST,
        port: process.env.REDIS_PORT,
        username: process.env.REDIS_USERNAME,
        password: process.env.REDIS_PASSWORD,
    },
    opensearch: {
        url: process.env.OPENSEARCH_URL,
        username: process.env.OPENSEARCH_USERNAME,
        password: process.env.OPENSEARCH_PASSWORD,
    },
};
