const client = require("../openSearchClient");

const INDEX = "csv_uploads";

async function ensureIndex() {
  try {
    const exists = await client.indices.exists({ index: INDEX });
    if (!exists) {
      await client.indices.create({
        index: INDEX,
        body: {
          mappings: {
            properties: {
              filename: { type: "keyword" },
              path: { type: "keyword" },
              url: { type: "keyword" },
              size: { type: "long" },
              mimetype: { type: "keyword" },
              uploader: { type: "keyword" },
              uploadedAt: { type: "date" },
              extra: { type: "object", enabled: true },
            },
          },
        },
      });
    }
  } catch (e) {
    // do not crash app on index init
    console.error("[csvAdapter.ensureIndex] error:", e.message);
  }
}

ensureIndex();

async function saveMeta(doc) {
  const res = await client.index({
    index: INDEX,
    document: doc,
    refresh: "wait_for",
  });
  return { id: res._id };
}

module.exports = { saveMeta };
