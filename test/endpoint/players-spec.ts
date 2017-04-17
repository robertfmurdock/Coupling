"use strict";
import * as Promise from "bluebird";
import CouplingDataService from "../../server/lib/CouplingDataService";
import Comparators from "../../common/Comparators";
import * as supertest from "supertest";
import * as monk from "monk";

let config = require('./../../config');
let tribeId = 'endpointTest';
let server = 'http://localhost:' + config.port;
let agent = supertest.agent(server);
let path = '/api/' + tribeId + '/players';

let database = monk(config.tempMongoUrl);
let playersCollection = database.get('players');

function clean(object) {
    return JSON.parse(JSON.stringify(object));
}

describe(path, function () {

    let couplingServer = agent;

    beforeEach(function (done) {
        couplingServer.get('/test-login?username="name"&password="pw"')
            .expect(302)
            .then(done, done.fail);
    });

    afterEach(function (done) {
        playersCollection.remove({tribe: tribeId}, false)
            .then(done, done.fail);
    });

    describe("GET", function () {
        it('will return all available players on team.', function (done) {
            let service = new CouplingDataService(config.tempMongoUrl);

            Promise.props({
                expected: service.requestPlayers(tribeId),
                response: couplingServer.get(path)
                    .expect(200)
                    .expect('Content-Type', /json/)
            })
                .then(function (props: any) {
                    expect(props.response.body).toEqual(props.expected);
                })
                .then(done, done.fail);
        });

        it('for retired players returns all retired players', function (done) {
            let service = new CouplingDataService(config.tempMongoUrl);
            let newPlayer = {_id: monk.id(), name: "Retiree", tribe: tribeId};

            couplingServer.post(path)
                .send(newPlayer)
                .then(function (responseContainingTheNewId) {
                    newPlayer = responseContainingTheNewId.body;
                })
                .then(function() {
                    couplingServer.delete(path + "/" + newPlayer._id)
                        .expect(200)
                })
                .then(function() {
                    return Promise.props({
                        expected: service.requestRetiredPlayers(tribeId),
                        response: couplingServer.get(path + '/retired')
                            .expect(200)
                            .expect('Content-Type', /json/)
                    });
                })
                .then(function(props: any) {
                    expect(props.response.body).toEqual(props.expected);
                })
                .then(done, done.fail);
        });
    });

    describe("POST", function () {

        it('will add player to tribe', function (done) {
            let newPlayer = clean({_id: monk.id(), name: "Awesome-O", tribe: tribeId});
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
                .expect({message: 'Failed to remove the player because it did not exist.'})
                .then(done, done.fail);
        });
    });
});

