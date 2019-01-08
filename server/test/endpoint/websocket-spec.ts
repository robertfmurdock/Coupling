import * as WebSocket from "ws";
import * as Promise from "bluebird";
import * as supertest from "supertest";
import * as monk from "monk";
import Tribe from "../../../common/Tribe";

let config = require('../../config/config');
let server = 'localhost:' + config.port;
let agent = supertest.agent(server);
let userEmail = 'test@test.tes';

let database = monk.default(config.tempMongoUrl);
let tribesCollection = database.get('tribes');
let playersCollection = database.get('players');
let usersCollection = monk.default(config.mongoUrl).get('users');

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

    const tribe: Tribe &  DatabaseEntity = {id: 'LOL', name: 'League of Losers', _id: undefined};
    const unauthorizedTribe: Tribe &  DatabaseEntity = {
        id: 'ROFL',
        name: 'Royal Observatory of Fluid Lightning',
        _id: undefined
    };

    function authorizeUserForTribes(authorizedTribes) {
        return usersCollection.update({email: userEmail + "._temp"}, {$set: {tribes: authorizedTribes}});
    }

    const tribeA = {id: 'Tribe A', name: 'AAAAA'};
    const tribeB = {id: 'Tribe B', name: 'BBBBB'};
    const tribeC = {id: 'Tribe C', name: 'CCCCC'};
    const tribes = [tribeA, tribeB, tribeC];

    beforeAll(function (done) {
        agent.get('/test-login?username=' + userEmail + '&password=pw')
            .expect(302)
            .then(response => this.authenticatedHeaders = {cookie: response.headers['set-cookie']})
            .then(() => Promise.all([
                playersCollection.drop(),
                tribesCollection.drop()
            ]))
            .then(() => tribesCollection.insert([tribe, unauthorizedTribe, ...tribes]))
            .then(() => authorizeUserForTribes([tribe.id, tribeA.id, tribeB.id, tribeC.id]))
            .then(done, done.fail);

        this.promiseWebsocket = function (tribeId = tribe.id) {
            return new Promise((resolve, reject) => {
                const options = {headers: this.authenticatedHeaders};
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

    it('when you are the only connection, gives you a count of one', function (done) {
        new Promise((resolve, reject) => {
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
            .timeout(100)
            .then((message) => {
                expect(message).toEqual(makeConnectionMessage(1));
            })
            .then(done, done.fail);
    });

    it('when there are multiple connections, gives you the total connection count', function (done) {
        Promise.all([this.promiseWebsocket(), this.promiseWebsocket()])
            .then(bundles => Promise.all(bundles.concat(this.promiseWebsocket())))
            .then(bundles => {
                expect(bundles[2].messages).toEqual([makeConnectionMessage(3)]);
                return bundles;
            })
            .then(closeAllSockets())
            .then(done, done.fail);
    });

    it('when there are multiple connections, gives you the total connection count for the current tribe', function (done) {
        Promise.all([
            this.promiseWebsocket(tribeA.id),
            this.promiseWebsocket(tribeB.id),
        ])
            .then(bundles => Promise.all(bundles.concat([
                this.promiseWebsocket(tribeA.id),
                this.promiseWebsocket(tribeB.id),
                this.promiseWebsocket(tribeB.id),
            ])))
            .then(bundles => Promise.all(bundles.concat([
                this.promiseWebsocket(tribeC.id),
                this.promiseWebsocket(tribeB.id),
            ])))
            .then(bundles => {
                const lastTribeAConnection = bundles[2];
                expect(lastTribeAConnection.messages).toEqual([makeConnectionMessage(2)]);

                const lastTribeBConnection = bundles[bundles.length - 1];
                expect(lastTribeBConnection.messages).toEqual([makeConnectionMessage(4)]);

                const lastTribeCConnection = bundles[5];
                expect(lastTribeCConnection.messages).toEqual([makeConnectionMessage(1)]);

                return bundles;
            })
            .then(closeAllSockets())
            .then(done, done.fail);
    });

    function makeConnectionMessage(count: number) {
        return 'Users viewing this page: ' + count;
    }

    let promiseWebsocketClose = function (bundle) {
        return new Promise(resolve => {
            bundle.websocket.on('close', () => resolve());
            bundle.websocket.close();
        })
            .timeout(100);
    };

    function closeAllSockets() {
        return bundles => {
            return Promise.all(bundles.map(bundle => promiseWebsocketClose(bundle)));
        }
    }

    it('starting with multiple connections then closing one updates the total connection count', function (done) {
        Promise.all([this.promiseWebsocket(), this.promiseWebsocket()])
            .then(bundles => {
                return promiseWebsocketClose(bundles[0])
                    .then(() => {
                        const newBundles = [bundles[1]];
                        return Promise.all(newBundles.concat(this.promiseWebsocket()));
                    });
            })
            .then(bundles => {
                expect(bundles[1].messages).toEqual([makeConnectionMessage(2)]);
                return bundles;
            })
            .then(closeAllSockets())
            .then(done, done.fail);
    });

    it('when a new connection is open existing connections receive a message with the new count', function (done) {
        this.promiseWebsocket()
            .then(bundle => Promise.all([bundle, this.promiseWebsocket()]))
            .then(bundles => {
                expect(bundles[0].messages).toEqual([makeConnectionMessage(1), makeConnectionMessage(2)]);
                return bundles;
            })
            .then(closeAllSockets())
            .then(done, done.fail);
    });

    it('when a connection closes existing connections receive a message with the new count', function (done) {
        this.promiseWebsocket()
            .then(bundle => {
                return Promise.all([bundle, this.promiseWebsocket()])
            })
            .then(bundles => {
                const [bundleToClose, openBundle] = bundles;
                return Promise.all([
                    promiseWebsocketClose(bundleToClose),
                    new Promise((resolve) => openBundle.websocket.on('message', resolve))
                        .timeout(100)
                ])
                    .then(() => openBundle);
            })
            .then(bundle => {
                expect(bundle.messages).toEqual([makeConnectionMessage(2), makeConnectionMessage(1)]);
                return [bundle];
            })
            .then(closeAllSockets())
            .then(done, done.fail);
    });

    it('does not talk to you if you are not authenticated', function (done) {
        const socketPromise = new Promise((resolve, reject) => {
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
        const socketPromise = new Promise((resolve, reject) => {
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
        new Promise((resolve, reject) => {
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
        new Promise(resolve => {
            const options = {headers: this.authenticatedHeaders};
            const websocket = new WebSocket(`ws://${server}/api/${tribe.id}/pairAssignments/current`, options);
            websocket.on('open', () => websocket.close());
            websocket.on('close', resolve);
        }).timeout(100)
            .then(() => agent.get('/test-login?username=' + userEmail + '&password=pw').expect(302))
            .then(done, done.fail)
    });

});