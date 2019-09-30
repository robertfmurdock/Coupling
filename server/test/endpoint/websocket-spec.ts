import * as WebSocket from "ws";
import * as Bluebird from "bluebird";
import * as supertest from "supertest";
import * as monk from "monk";
import Tribe from "../../lib/common/Tribe";

let config = require('../../config/config');
let server = 'localhost:' + config.port;
let userEmail = 'test@test.tes';
let userEmailTemp = 'test@test.tes._temp';
const userPlayer = {_id: '-1', name: 'test', email: userEmailTemp,};

let database = monk.default(config.tempMongoUrl);
let tribesCollection = database.get('tribes');
let playersCollection = database.get('players');
let usersCollection = monk.default(config.mongoUrl).get('users');

async function getAuthenticatedCookie(username: any) {
    const response = await supertest.agent(server).get(`/test-login?username=${username}&password=pw`).expect(302);
    return {cookie: response.headers['set-cookie']};
}

describe('Current connections websocket', function () {

    function setupErrorHandler(websocket: WebSocket, reject) {
        websocket.on('error', error => {
            reject(error);
            websocket.close();
        });
    }

    interface DatabaseEntity {
        _id: string
    }

    const tribe: Tribe & DatabaseEntity = {id: 'LOL', name: 'League of Losers', _id: undefined};
    const unauthorizedTribe: Tribe & DatabaseEntity = {
        id: 'ROFL',
        name: 'Royal Observatory of Fluid Lightning',
        _id: undefined
    };

    async function authorizeUserForTribes(authorizedTribes, email: any) {
        await usersCollection.remove({email: email + "._temp"});
        await usersCollection.insert({email: email + "._temp", tribes: authorizedTribes, timestamp: new Date()});
    }

    const tribeA = {id: 'Tribe A', name: 'AAAAA'};
    const tribeB = {id: 'Tribe B', name: 'BBBBB'};
    const tribeC = {id: 'Tribe C', name: 'CCCCC'};
    const tribes = [tribeA, tribeB, tribeC];

    beforeAll(async function () {
        this.authenticatedHeaders = await getAuthenticatedCookie(userEmail);

        await Bluebird.all([
            playersCollection.drop(),
            tribesCollection.drop()
        ]);
        await tribesCollection.insert([tribe, unauthorizedTribe, ...tribes]);
        await authorizeUserForTribes([tribe.id, tribeA.id, tribeB.id, tribeC.id], userEmail);

        this.promiseWebsocket = function (tribeId = tribe.id, authenticatedHeaders = this.authenticatedHeaders) {
            return new Bluebird((resolve, reject) => {
                const options = {headers: authenticatedHeaders};
                const websocket = new WebSocket(`ws://${server}/api/${tribeId}/pairAssignments/current`, options);
                const messages = [];

                websocket.on('message', message => {
                    messages.push(message);
                    resolve({messages, websocket});
                });

                websocket.on('close', () => {
                    reject('Websocket closed');
                });

                setupErrorHandler(websocket, reject);
            }).timeout(100);
        };
    });

    it('when you are the only connection, gives you a count of one', async function () {
        await new Bluebird((resolve, reject) => {
            const options = {headers: this.authenticatedHeaders};
            const websocket = new WebSocket(`ws://${server}/api/${tribe.id}/pairAssignments/current`, options);

            websocket.on('message', message => {
                resolve(message);
                websocket.close();
            });

            websocket.on('close', () => {
                reject('Websocket closed');
            });

            setupErrorHandler(websocket, reject);
        })
            .timeout(1000)
            .then((message) => {
                expect(message).toEqual(makeConnectionMessage(1, [userPlayer]));
            })
    });

    it('when there are multiple connections, gives you the total connection count', function (done) {
        Bluebird.all([this.promiseWebsocket(), this.promiseWebsocket()])
            .then(bundles => Bluebird.all(bundles.concat(this.promiseWebsocket())))
            .then(bundles => {
                expect(bundles[2].messages).toEqual([(makeConnectionMessage(3, [userPlayer]))]);
                return bundles;
            })
            .then(closeAllSockets())
            .then(done, done.fail);
    });

    it('when there are multiple connections, gives you the total connection count for the current tribe', async function () {
        const altEmail = "alt-email@email.edu";
        const altAuthenticatedHeaders = await getAuthenticatedCookie(altEmail);
        await authorizeUserForTribes([tribeB.id], altEmail);
        const altUserPlayerId = monk.id();
        await playersCollection.insert({
            _id: altUserPlayerId,
            tribe: tribeB.id,
            name: "excellentPlayer",
            email: altEmail + "._temp"
        });

        let messages = await Bluebird.all([
            this.promiseWebsocket(tribeA.id),
            this.promiseWebsocket(tribeB.id),
        ]);
        messages = messages.concat(await Bluebird.all([
            this.promiseWebsocket(tribeA.id),
            this.promiseWebsocket(tribeB.id, altAuthenticatedHeaders),
            this.promiseWebsocket(tribeB.id),
        ]));
        messages = messages.concat(await Bluebird.all([
            this.promiseWebsocket(tribeC.id),
            this.promiseWebsocket(tribeB.id),
        ]));
        const lastTribeAConnection = messages[2];
        expect(lastTribeAConnection.messages)
            .toEqual([(makeConnectionMessage(2, [userPlayer]))]);

        const lastTribeBConnection = messages[messages.length - 1];
        expect(lastTribeBConnection.messages)
            .toEqual([makeConnectionMessage(4, [userPlayer, {
                _id: altUserPlayerId,
                name: "excellentPlayer",
                email: altEmail + "._temp"
            }])]);

        const lastTribeCConnection = messages[5];
        expect(lastTribeCConnection.messages)
            .toEqual([(makeConnectionMessage(1, [userPlayer]))]);
        await closeAllSockets();
    });

    function makeConnectionMessage(count: number, players: any) {
        return JSON.stringify({
            type: "LivePlayers",
            text: 'Users viewing this page: ' + count,
            players: players
        })
    }

    let promiseWebsocketClose = function (bundle) {
        return new Bluebird(resolve => {
            bundle.websocket.on('close', () => resolve());
            bundle.websocket.close();
        })
            .timeout(100);
    };

    function closeAllSockets() {
        return bundles => {
            return Bluebird.all(bundles.map(bundle => promiseWebsocketClose(bundle)));
        }
    }

    it('starting with multiple connections then closing one updates the total connection count', function (done) {
        Bluebird.all([this.promiseWebsocket(), this.promiseWebsocket()])
            .then(bundles => {
                return promiseWebsocketClose(bundles[0])
                    .then(() => {
                        const newBundles = [bundles[1]];
                        return Bluebird.all(newBundles.concat(this.promiseWebsocket()));
                    });
            })
            .then(bundles => {
                expect(bundles[1].messages)
                    .toEqual([(makeConnectionMessage(2, [userPlayer]))]);
                return bundles;
            })
            .then(closeAllSockets())
            .then(done, done.fail);
    });

    it('when a new connection is open existing connections receive a message with the new count', function (done) {
        this.promiseWebsocket()
            .then(bundle => Bluebird.all([bundle, this.promiseWebsocket()]))
            .then(bundles => {
                expect(bundles[0].messages)
                    .toEqual([(makeConnectionMessage(1, [userPlayer])),
                        (makeConnectionMessage(2, [userPlayer]))]);
                return bundles;
            })
            .then(closeAllSockets())
            .then(done, done.fail);
    });

    it('when a connection closes existing connections receive a message with the new count', function (done) {
        this.promiseWebsocket()
            .then(bundle => {
                return Bluebird.all([bundle, this.promiseWebsocket()])
            })
            .then(bundles => {
                const [bundleToClose, openBundle] = bundles;
                return Bluebird.all([
                    promiseWebsocketClose(bundleToClose),
                    new Bluebird((resolve) => openBundle.websocket.on('message', resolve))
                        .timeout(100)
                ])
                    .then(() => openBundle);
            })
            .then(bundle => {
                expect(bundle.messages)
                    .toEqual([(makeConnectionMessage(2, [userPlayer])),
                        (makeConnectionMessage(1, [userPlayer]))]);
                return [bundle];
            })
            .then(closeAllSockets())
            .then(done, done.fail);
    });

    it('does not talk to you if you are not authenticated', function (done) {
        const socketPromise = new Bluebird((resolve, reject) => {
            const unauthenticatedHeaders = {};
            const websocket = new WebSocket(`ws://${server}/api/${tribe.id}/pairAssignments/current`, unauthenticatedHeaders);
            websocket.on('close', resolve);
            setupErrorHandler(websocket, reject);
        });

        socketPromise
            .timeout(100)
            .then(done, done.fail);
    });

    it('does not talk to you if are not authorized for the tribe requested', function (done) {
        const socketPromise = new Bluebird((resolve, reject) => {
            const options = {headers: this.authenticatedHeaders};
            const websocket = new WebSocket(`ws://${server}/api/${unauthorizedTribe.id}/pairAssignments/current`, options);
            websocket.on('close', resolve);
            setupErrorHandler(websocket, reject);
        });

        socketPromise
            .timeout(100)
            .then(done, done.fail);
    });

    it('going to a socket location which does not exist will not crash the server', function (done) {
        new Bluebird((resolve, reject) => {
            const options = {headers: this.authenticatedHeaders};
            const websocket = new WebSocket(`ws://${server}/api/404WTF`, options);

            websocket.on('close', () => {
                resolve('Websocket closed');
            });

            setupErrorHandler(websocket, reject);
        }).timeout(100)
            .then(done, done.fail)

    });

    it('server will not crash when socket is immediately closed', function (done) {
        new Bluebird(resolve => {
            const options = {headers: this.authenticatedHeaders};
            const websocket = new WebSocket(`ws://${server}/api/${tribe.id}/pairAssignments/current`, options);
            websocket.on('open', () => websocket.close());
            websocket.on('close', resolve);
        }).timeout(100)
            .then(() => supertest.agent(server).get(`/test-login?username=${userEmail}&password=pw`).expect(302))
            .then(done, done.fail)
    });

})
;