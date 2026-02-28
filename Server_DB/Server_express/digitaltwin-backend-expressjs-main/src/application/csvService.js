const repo = require("../domain/repository/csvRepository");

async function recordUpload({ filename, path, url, size, mimetype, uploader, extra }) {
  const uploadedAt = new Date().toISOString();
  return await repo.saveCsvMeta({ filename, path, url, size, mimetype, uploader, uploadedAt, extra });
}

module.exports = { recordUpload };
