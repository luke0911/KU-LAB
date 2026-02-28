const csvAdapter = require("../../infra/db/repository/csvAdapter");

async function saveCsvMeta(meta) {
  return await csvAdapter.saveMeta(meta);
}

module.exports = { saveCsvMeta };
