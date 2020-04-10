const msConfig = require('./azure-ad-config').default;

var config = {
  get publicUrl() {
    return process.env.PUBLIC_URL || 'http://localhost:' + config.port;
  },
  get publicHost() {
    return process.env.PUBLIC_HOST || 'localhost';
  },
  get port() {
    return process.env.PORT || 3000;
  },
  get secret() {
    return "maythefourthbewithyou";
  },
  get googleClientID() {
    return process.env.GOOGLE_CLIENT_ID || '24452716216-9lqe1p511qcf53kuihamdhggb05gbt4p.apps.googleusercontent.com';
  },
  get googleClientSecret() {
    return process.env.GOOGLE_CLIENT_SECRET || 'ZVTj-iV5ZzW3-6so_1Q-bSPQ';
  },
  get gitRev() {
      return 'None';
  },
  get buildDate() {
      return 'None';
  },
  microsoft: msConfig
};

module.exports = config;
