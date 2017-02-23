import * as WebSocket from "ws";
import * as Promise from "bluebird";
import * as supertest from "supertest";

let config = require('../../config');
let server = 'localhost:' + config.port;
let agent = supertest.agent(server);
let userEmail = 'test@test.tes';

describe('Current connections websocket', function () {

    function setupErrorHandler(websocket: WebSocket, reject) {
        websocket.on('error', error => {
            reject(error);
            websocket.close();
        });
    }

    beforeAll(function (done) {
        agent.get('/test-login?username=' + userEmail + '&password=pw')
            .expect(302)
            .then(response => this.authenticatedHeaders = {cookie: response.headers['set-cookie']})
            .then(done, done.fail);
    });

    it('gives you a friendly greeting but no useful information', function (done) {
        new Promise((resolve, reject) => {
            const options = {headers: this.authenticatedHeaders};
            const websocket = new WebSocket(`ws://${server}/api/LOL/pairAssignments/current`, options);

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
                expect(message).toEqual('Connected');
            })
            .then(done, done.fail);
    });

    it('does not talk to you if you are not authenticated', function (done) {
        const socketPromise = new Promise((resolve, reject) => {
            const unauthenticatedHeaders = {};
            const websocket = new WebSocket(`ws://${server}/api/LOL/pairAssignments/current`, unauthenticatedHeaders);
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
            const websocket = new WebSocket(`ws://${server}/api/LOL/pairAssignments/current`, options);
            websocket.on('open', () => websocket.close());
            websocket.on('close', resolve);
        }).timeout(100)
            .then(() => agent.get('/test-login?username=' + userEmail + '&password=pw').expect(302))
            .then(done, done.fail)
    });

});