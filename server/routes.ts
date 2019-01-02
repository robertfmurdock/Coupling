import apiGuard from "./routes/api-guard";
import * as passport from "passport";
import * as routes from "./routes/index";
import tribeRoute from "./routes/tribeRoute";
import tribeListRoute from "./routes/tribeListRoute";
import * as WebSocket from "ws";
import * as AuthorizedTribeFetcher from "./lib/AuthorizedTribesFetcher";

module.exports = function (wsInstance, userDataService, couplingDataService) {

    const app = wsInstance.app;

    app.get('/welcome', routes.welcome);
    app.post('/auth/google-token', passport.authenticate('custom'), ((req, res) => res.sendStatus(200)));

    if ('development' == app.get('env') || 'test' == app.get('env')) {
        app.get('/test-login', passport.authenticate('local', {successRedirect: '/', failureRedirect: '/login'}));
    }

    app.get('/', routes.index);
    app.all('/api/*', apiGuard(userDataService, couplingDataService));
    app.use('/api/tribes', tribeListRoute);
    app.use('/api/:tribeId', tribeRoute);
    app.get('/app/*.html', routes.components);
    app.get('/partials/:name', routes.partials);

    app.ws('/api/:tribeId/pairAssignments/current', (connection, request) => {

        console.log('Websocket connection count: ' + wsInstance.getWss().clients.size);

        AuthorizedTribeFetcher.promiseTribeAndAuthorization(request)
            .then(({isAuthorized}) => {
                if (isAuthorized) {
                    const tribeId = request.params.tribeId;
                    broadcastConnectionCountForTribe(tribeId);

                    connection.on('close', () => broadcastConnectionCountForTribe(tribeId));
                    connection.on('error', console.log);
                } else {
                    connection.close();
                }
            });
    });

    function broadcast(message: string, clients: WebSocket[]) {
        console.log('Broadcasting. Total connections: ' + wsInstance.getWss().clients.size);
        clients.forEach((client: WebSocket) => client.send(message));
    }

    let connectionIsOpenAndForSameTribe = function (client, tribeId) {
        return client.readyState === WebSocket.OPEN && client.upgradeReq.params.tribeId === tribeId;
    };

    let broadcastConnectionCountForTribe = function (tribeId) {
        const clients = wsInstance.getWss().clients;
        const matchingConnections = [];
        clients.forEach(client => {
            if (connectionIsOpenAndForSameTribe(client, tribeId)) {
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