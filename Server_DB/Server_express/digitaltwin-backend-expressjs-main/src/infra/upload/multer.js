const fs = require("fs");
const path = require("path");
const multer = require("multer");

const csvDir = path.join(__dirname, "..", "..", "public", "csvs");

// ensure dir
if (!fs.existsSync(csvDir)) {
  fs.mkdirSync(csvDir, { recursive: true });
}

const storage = multer.diskStorage({
  destination: function (req, file, cb) {
    cb(null, csvDir);
  },
  filename: function (req, file, cb) {
    const ts = new Date().toISOString().replace(/[:.]/g, "-");
    const safe = file.originalname.replace(/[^\w.\-]/g, "_");
    cb(null, `${ts}__${safe}`);
  },
});

const fileFilter = (req, file, cb) => {
  // mime type guard (text/csv or application/vnd.ms-excel often used)
  const ok =
    file.mimetype === "text/csv" ||
    file.mimetype === "application/vnd.ms-excel" ||
    /\.csv$/i.test(file.originalname);
  if (!ok) return cb(new Error("CSV only"), false);
  cb(null, true);
};

const upload = multer({ storage, fileFilter, limits: { fileSize: 50 * 1024 * 1024 } }); // 50MB

module.exports = { upload, csvDir };
