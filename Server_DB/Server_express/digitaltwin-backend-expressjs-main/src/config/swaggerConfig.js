const swaggerJSDoc = require('swagger-jsdoc');
const path = require('path');
const { host, basePath } = require('../config/env');

const options = {
  definition: {
    openapi: '3.0.0',
    info: {
      title: 'Fifth Dimension Digital Twin API',
      version: '1.0.0',
      description: 'Digital Twin Express 프로젝트 API Docs',
    },
    servers: [
      {
        url: `${host}${basePath}`,
        description: 'Prod',
      },
      {
        url: `http://localhost:3000${basePath}`,
        description: 'Dev',
      },
    ],
    components: {
      securitySchemes: {
        'JWT-Token': {
          type: 'http',
          scheme: 'bearer',
          bearerFormat: 'JWT',
          name: 'Authorization',
          in: 'header',
          description: 'JWT Authorization header using the Bearer scheme. Example: "Authorization: Bearer {token}"'
        }
      }
    },
    security: [
      {
        'JWT-Token': []
      }
    ]
  },
  apis: [path.join(__dirname, '../presentation/*.js')],
};

const specs = swaggerJSDoc(options);
module.exports = specs;