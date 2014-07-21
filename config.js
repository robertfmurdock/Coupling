var config = {
    get publicUrl() {
        return process.env.PUBLIC_URL || 'http://localhost:' + config.port;
    },
    get port() {
        return process.env.PORT || 3000;
    },
    get mongoUrl() {
        return  process.env.MONGOHQ_URL_MONGOURL || "localhost/Coupling";
    },
    get secret() {
        return  "maythefourthbewithyou";
    },
    get requiresAuthentication() {
        return true;
    },
    get googleClientID() {
        return process.env.GOOGLE_CLIENT_ID || '24452716216-9lqe1p511qcf53kuihamdhggb05gbt4p.apps.googleusercontent.com';
    },
    get googleClientSecret() {
        return process.env.GOOGLE_CLIENT_SECRET || 'ZVTj-iV5ZzW3-6so_1Q-bSPQ';
    },
    get gitRev() {
        try {
            var fileJSON = require('./version');
            return fileJSON.gitRev;
        } catch (err) {
            return 'None';
        }
    },
    get buildDate() {
        try {
            var fileJSON = require('./version');
            return fileJSON.date;
        } catch (err) {
            return 'None';
        }
    }
};
module.exports = config;
