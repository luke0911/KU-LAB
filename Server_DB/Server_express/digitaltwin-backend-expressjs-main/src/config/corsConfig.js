const { host } = require('../config/env');
const allowedOrigins = ['http://localhost:5000', 'https://localhost:5000', 'http://localhost:3000','https://localhost:3000', `${host}`];

const corsOptions = {
  origin: function (origin, callback) {
    // origin이 없거나(예: curl, Postman) 그냥 다 허용
    callback(null, true);
  },
  credentials: false,
};


module.exports = corsOptions;