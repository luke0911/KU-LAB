const { Client } = require('@opensearch-project/opensearch');
const { opensearch: opensearchConfig } = require('../../config/env');

const client = new Client({
  node: opensearchConfig.url,
  auth: {
    username: opensearchConfig.username, // 사용자명
    password: opensearchConfig.password, // 비밀번호
  },
  maxRetries: 5,
  requestTimeout: 60000,
  // 커넥션 풀 관련 옵션
  agent: {
    maxSockets: 50,        // 최대 동시 커넥션 수
    keepAlive: true,
    keepAliveMsecs: 1000,
  }
});

async function pingOpenSearch() {
  try {
    await client.ping();
    console.log('OpenSearch connected');
  } catch (error) {
    console.error('OpenSearch connection failed:', error.meta?.body || error);
  }
}

module.exports = { osClient: client, pingOpenSearch };
