module.exports = {
    success: (statusCode = 200, data = null, message = null) => ({
      statusCode,
      data,
      message,
    }),
    error: (statusCode = 400, message = 'Error') => ({
      statusCode,
      message,
    }),
  };