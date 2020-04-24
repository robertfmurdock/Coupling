import * as passport from "passport";
import * as routes from "./index";
// @ts-ignore
import * as server from "Coupling-server";

const {websocketRoute} = server.com.zegreatrob.coupling.server.route;
const {configRoutes} = server.com.zegreatrob.coupling.server;

module.exports = function (wsInstance) {
    const app = wsInstance.app;

    app.post('/auth/google-token', passport.authenticate('custom'), ((req, res) => res.sendStatus(200)));
    app.get('/microsoft-login', passport.authenticate('azuread-openidconnect'));
    app.post('/auth/signin-microsoft',
        passport.authenticate('azuread-openidconnect', {failureRedirect: '/'}), (req, res) => res.redirect('/'));

    const expressEnv = app.get('env');
    const isInDevMode = 'development' == expressEnv || 'test' == expressEnv;
    if (isInDevMode) {
        app.get('/test-login', passport.authenticate('local', {successRedirect: '/', failureRedirect: '/login'}));
    }

    const indexRoute = routes.index(expressEnv);
    configRoutes(expressEnv, app, wsInstance);

};