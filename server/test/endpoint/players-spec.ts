"use strict";
import Comparators from "../../../common/Comparators";
import * as supertest from "supertest";
import * as monk from "monk";

let config = require('../../config/config');
let tribeId = 'endpointTest';
let server = 'http://localhost:' + config.port;
let agent = supertest.agent(server);
let path = '/api/' + tribeId + '/players';

let database = monk.default(config.tempMongoUrl);
let playersCollection = database.get('players');

let usersCollection = monk.default(config.mongoUrl).get('users');

function clean(object) {
    return JSON.parse(JSON.stringify(object));
}

describe(path, function () {
    let userEmail = 'test@test.tes';
    let couplingServer = agent;

    beforeEach(async function () {
        await couplingServer.get(`/test-login?username=${userEmail}&password=pw`)
            .expect(302);
        await authorizeUserForTribes([tribeId]);
    });

    function authorizeUserForTribes(authorizedTribes) {
        return usersCollection.insert({email: userEmail + "._temp", tribes: authorizedTribes, timestamp: new Date()});
    }

    afterEach(async function () {
        await playersCollection.remove({tribe: tribeId}, false)
    });

    describe("GET", function () {

        it('is not allowed for users without access', async function () {
            await couplingServer.get('/api/somebodyElsesTribe/players')
                .expect(404)
        });

        it('will return all available players on team.', async function () {
            let newPlayer1 = clean({
                _id: monk.id(),
                name: "Awesome-O",
                callSignAdjective: 'Awesome',
                callSignNoun: 'Sauce'
            });
            let newPlayer2 = clean({
                _id: monk.id(),
                name: "Awesome-O-2",
                callSignAdjective: 'Very',
                callSignNoun: 'Ok'
            });
            await couplingServer.post(path).send(newPlayer1).expect(200);
            await couplingServer.post(path).send(newPlayer2).expect(200);

            const response = await couplingServer.get(path)
                .expect(200)
                .expect('Content-Type', /json/);

            expect(response.body).toEqual([newPlayer1, newPlayer2]);
        });

        it('for retired players returns all retired players', async function () {
            let newPlayer = {_id: monk.id().toString(), name: "Retiree"};

            await couplingServer.post(path).send(newPlayer);
            await couplingServer.delete(path + "/" + newPlayer._id).expect(200);
            const response = await couplingServer.get(path + '/retired')
                .expect(200)
                .expect('Content-Type', /json/);

            expect(response.body).toEqual([newPlayer]);
        });
    });

    describe("POST", function () {

        it('will add player to tribe', function (done) {
            let newPlayer = clean({_id: monk.id(), name: "Awesome-O", callSignAdjective: 'Super', callSignNoun: 'Hot'});
            let httpPost = couplingServer.post(path);
            httpPost.send(newPlayer)
                .expect(200, newPlayer)
                .then(function () {
                    return couplingServer.get(path)
                        .expect('Content-Type', /json/)
                        .expect(200)
                })
                .then(function (response) {
                    expect(response.body).toEqual([newPlayer]);
                })
                .then(done, done.fail);
        });

        it('is not allowed for users without access', async function () {
            await couplingServer.post('/api/somebodyElsesTribe/players')
                .send({name: 'new player'})
                .expect(404)
        });

    });

    describe("DELETE", function () {

        let newPlayer = {_id: monk.id(), name: "Awesome-O", tribe: tribeId};

        beforeEach(function (done) {
            couplingServer.post(path)
                .send(newPlayer)
                .then(function (responseContainingTheNewId) {
                    newPlayer = responseContainingTheNewId.body;
                })
                .then(done, done.fail);
        });

        it('will remove a given player.', function (done) {
            let httpDelete = couplingServer.delete(path + "/" + newPlayer._id);
            httpDelete.expect(200, function () {
                couplingServer.get(path)
                    .then(function (response) {
                        let result = response.body.some(function (player) {
                            return Comparators.areEqualPlayers(newPlayer, player);
                        });
                        expect(result).toBe(false);
                    })
                    .then(done, done.fail);
            });
        });

        it('will return an error when the player does not exist.', function (done) {
            let badId = monk.id();
            let httpDelete = couplingServer.delete(path + "/" + badId);
            httpDelete
                .expect(404)
                .expect({message: 'Player could not be deleted because they do not exist.'})
                .then(done, done.fail);
        });

        it('is not allowed for users without access', async function () {
            await couplingServer.delete('/api/somebodyElsesTribe/players/playerId')
                .expect(404)
        });
    });
});

