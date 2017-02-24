import apiGuard from "./routes/api-guard";
import * as passport from "passport";
import * as routes from "./routes/index";
import tribeRoute from "./routes/tribeRoute";
import tribeListRoute from "./routes/tribeListRoute";
import * as WebSocket from "ws";

const config = require('./../config');

module.exports = function (wsInstance, userDataService, couplingDataService) {

    const app = wsInstance.app;

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

    app.ws('/api/:tribeId/pairAssignments/current', (connection, request) => {
        const tribeId = request.params.tribeId;
        broadcastConnectionCountForTribe(tribeId);

        connection.on('close', () => broadcastConnectionCountForTribe(tribeId));
        connection.on('error', console.log);
    });

    function broadcast(message: string, clients: WebSocket[]) {
        clients.forEach((client: WebSocket) => client.send(message));
    }

    let connectionIsOpenAndForSameTribe = function (client, tribeId) {
        return client.readyState === WebSocket.OPEN && client.upgradeReq.params.tribeId === tribeId;
    };

    let broadcastConnectionCountForTribe = function (tribeId) {
        const clients = wsInstance.getWss().clients;

        const matchingConnections = [];
        clients.forEach(client => {
            if(connectionIsOpenAndForSameTribe(client, tribeId)) {
                matchingConnections.push(client);
            }
        });

        broadcast('Users viewing this page: ' + matchingConnections.length, matchingConnections);
    };

    app.ws('*', (ws) => {
        ws.close();
    });

    app.get('*', routes.index);

};