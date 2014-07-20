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
        return process.env.GOOGLE_CLIENT_ID || '1043188507506-kg32jar07pr5eir8f7ee4gffock7i7sq.apps.googleusercontent.com';
    },
    get googleClientSecret() {
        return process.env.GOOGLE_CLIENT_SECRET || 'q-GTcE4Cv7l_wex2ITdF6VmG';
    }
};
module.exports = config;
