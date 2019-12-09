import apiGuard from "./routes/api-guard";
import * as passport from "passport";
import * as routes from "./routes/index";
// @ts-ignore
import * as server from "Coupling-server";

const {tribeListRouter, websocketRoute} = server.com.zegreatrob.coupling.server.route;

module.exports = function (wsInstance, userDataService, couplingDataService) {
    const app = wsInstance.app;

    app.get('/api/logout', function (req, res) {
        req.logout();
        res.send('ok')
    });

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
    app.get('/', indexRoute);
    app.all('/api/*', apiGuard(userDataService, couplingDataService));
    app.use('/api/tribes', tribeListRouter);
    app.get('/app/*.html', routes.components);
    app.get('/partials/:name', routes.partials);

    app.ws('/api/:tribeId/pairAssignments/current', (connection, request) => {
        websocketRoute(connection, request, wsInstance.getWss())
    });

    app.ws('*', (ws) => {
        ws.close();
    });

    app.get('*', indexRoute);

};