import apiGuard from "./routes/api-guard";
import * as passport from "passport";
import * as routes from "./routes/";
import tribeRoute from './routes/tribeRoute'
import tribeListRoute from './routes/tribeListRoute'

var config = require('./../config');

module.exports = function (app, userDataService, couplingDataService) {
    app.get('/welcome', routes.welcome);
    app.get('/auth/google', passport.authenticate('google'));
    app.get('/auth/google/callback', passport.authenticate('google', {
        successRedirect: '/',
        failureRedirect: '/auth/google'
    }));
    if ('development' == app.get('env') || 'test' == app.get('env')) {
        app.get('/test-login', passport.authenticate('local', {successRedirect: '/', failureRedirect: '/login'}));
    }

    app.get('/', routes.index);
    app.all('/api/*', apiGuard(couplingDataService));
    app.use('/api/tribes', tribeListRoute);
    app.use('/api/:tribeId', tribeRoute);
    app.get('/app/*.html', routes.components);
    app.get('/partials/:name', routes.partials);
    app.get('*', routes.index);
};