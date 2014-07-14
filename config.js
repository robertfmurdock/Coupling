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
    }
};
module.exports = config;
