import apiGuard from "./routes/api-guard";
import * as passport from "passport";
import * as routes from "./routes/index";
import tribeRoute from "./routes/tribeRoute";
import tribeListRoute from "./routes/tribeListRoute";
import * as WebSocket from "ws";

const config = require('./../config');

module.exports = function (wsInstance, userDataService, couplingDataService) {

    const app = wsInstance.app;
    const clients = wsInstance.getWss().clients;

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

    app.ws('/api/LOL/pairAssignments/current', connection => {
        broadcastConnectionCount();

        connection.on('close', broadcastConnectionCount);
        connection.on('error', console.log);
    });

    function broadcast(message: string) {
        clients.forEach((client: WebSocket) => {
            if (client.readyState === WebSocket.OPEN) {
                client.send(message);
            }
        });
    }

    let broadcastConnectionCount = function () {
        broadcast('Number of connections: ' + clients.size);
    };

    app.ws('*', (ws) => {
        ws.close();
    });

    app.get('*', routes.index);

};