import apiGuard from "./routes/api-guard";
import * as passport from "passport";
import * as routes from "./routes/index";
import tribeRoute from "./routes/tribeRoute";
import tribeListRoute from "./routes/tribeListRoute";
import * as WebSocket from "ws";

function toUserPlayerList(matchingConnections, players) {
    const uniqueEmails = [...new Set(matchingConnections.map(it => (it.user.email)))];
    return uniqueEmails.map(email => {
        const existingPlayer = players.find(it => it.email === email);
        if (existingPlayer) {
            return existingPlayer
        } else {
            return {email: email};
        }
    });
}

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
    app.use('/api/tribes', tribeListRoute);
    app.use('/api/:tribeId', tribeRoute);
    app.get('/app/*.html', routes.components);
    app.get('/partials/:name', routes.partials);

    app.ws('/api/:tribeId/pairAssignments/current', async (connection, request) => {
        console.log('Websocket connection count: ' + wsInstance.getWss().clients.size);

        const result = await request.commandDispatcher.performUserIsAuthorizedWithDataAction(request.params.tribeId);
        if (result != null) {
            const tribeId = request.params.tribeId;
            connection.tribeId = tribeId;
            connection.user = request.user;
            broadcastConnectionCountForTribe(tribeId, result.players);

            connection.on('close', () => broadcastConnectionCountForTribe(tribeId, result.players));
            connection.on('error', console.log);
        } else {
            connection.close();
        }
    });

    function broadcast(message: string, clients: WebSocket[]) {
        clients.forEach((client: WebSocket) => client.send(message));
    }

    let connectionIsOpenAndForSameTribe = function (client, tribeId) {
        return client.readyState === WebSocket.OPEN && client.tribeId === tribeId;
    };

    let broadcastConnectionCountForTribe = function (tribeId, players) {
        const clients = wsInstance.getWss().clients;
        const matchingConnections = [];
        clients.forEach(client => {
            if (connectionIsOpenAndForSameTribe(client, tribeId)) {
                matchingConnections.push(client);
            }
        });

        broadcast(JSON.stringify(
            {
                type: "LivePlayers",
                text: 'Users viewing this page: ' + matchingConnections.length,
                players: toUserPlayerList(matchingConnections, players)
            }
        ), matchingConnections);
    };

    app.ws('*', (ws) => {
        ws.close();
    });

    app.get('*', indexRoute);

};