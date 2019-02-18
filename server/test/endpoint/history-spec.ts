"use strict";

import * as Bluebird from "bluebird";
import * as supertest from "supertest";
import * as monk from "monk";

let config = require('../../config/config');
let server = 'http://localhost:' + config.port;
let agent = supertest.agent(server);
let tribeId = 'endpointTest';
let path = '/api/' + tribeId + '/history';

let database = monk.default(config.tempMongoUrl);
let historyCollection = database.get('history');
let tribesCollection = database.get('tribes');

let usersCollection = monk.default(config.mongoUrl).get('users');

const userEmail = '"name"';

describe(path, function () {
    let validPairs = {
        date: new Date().toISOString(),
        pairs: [
            [
                {name: "Shaggy", pins: []},
                {name: "Scooby", pins: []}
            ]
        ],
        _id: monk.id().toString()
    };


    async function authorizeUserForTribes(authorizedTribes) {
        await usersCollection.remove({email: userEmail + "._temp"});
        await usersCollection.insert({email: userEmail + "._temp", tribes: authorizedTribes, timestamp: new Date()});
    }

    beforeAll(async function () {
        await historyCollection.remove({tribe: tribeId}, false);
        let tribe = {id: tribeId, name: 'tribe-from-endpoint-tests'};
        let tribe2 = {id: 'test2', name: 'alt-tribe-from-endpoint-tests'};
        await tribesCollection.insert([tribe, tribe2]);
        await authorizeUserForTribes([tribe.id, tribe2.id])
    });

    beforeEach(function (done) {
        agent.get('/test-login?username=' + userEmail + '&password="pw"')
            .expect(302)
            .then(done, done.fail);
    });

    afterEach(function (done) {
        historyCollection.remove({_id: validPairs._id}, false)
            .then(done, done.fail);
    });

    describe('GET', function () {

        beforeEach(function (done) {
            agent.post(path)
                .send(validPairs)
                .expect('Content-Type', /json/)
                .then(done, done.fail);
        });

        it('will show history of tribe that has history.', function (done) {
            agent.get(path)
                .expect('Content-Type', /json/)
                .then(function (response) {
                    expect(response.body).toEqual([validPairs]);
                })
                .then(done, done.fail);
        });

        it('will show history of tribe that has no history.', function (done) {
            agent.get('/api/test2/history')
                .expect('Content-Type', /json/)
                .then(function (response) {
                    expect([]).toEqual(response.body);
                })
                .then(done, done.fail);
        });

        it('is not allowed for users without access', async function () {
            await agent.get('/api/someoneElsesTribe/history')
                .expect(404)
        });
    });

    describe("POST will save pairs", function () {

        it('should add when given a valid pair assignment document.', function (done) {
            agent.post(path)
                .send(validPairs)
                .expect(200)
                .expect('Content-Type', /json/)
                .then(function (response) {
                    let pairsAsSaved = response.body;

                    return Bluebird.props(
                        {
                            pairsAsSaved: pairsAsSaved,
                            history: agent.get(path)
                                .expect(200)
                                .then(function (response) {
                                    return response.body
                                })
                        }
                    );
                })
                .then(function (props: any) {
                    let pairsAsSaved = props.pairsAsSaved;
                    let history = props.history;
                    let latestEntryInHistory = history[0];

                    expect(JSON.parse(JSON.stringify(pairsAsSaved)))
                        .toEqual(JSON.parse(JSON.stringify(latestEntryInHistory)));
                })
                .then(done, done.fail);
        });
        it('should not add when given a document without a date', function (done) {
            let pairs = {
                pairs: [
                    [
                        {name: "Shaggy"},
                        {name: "Scooby"}
                    ]
                ]
            };
            agent.post(path).send(pairs)
                .expect(400)
                .expect('Content-Type', /json/)
                .then(function (response) {
                    expect(response.body).toEqual({error: 'Pairs were not valid.'});
                })
                .then(done, done.fail);
        });
        it('should not add when given a document without pairs', function (done) {
            let pairs = {date: new Date()};
            agent
                .post(path)
                .send(pairs)
                .expect(400)
                .expect('Content-Type', /json/)
                .then(function (response) {
                    expect(response.body).toEqual({error: 'Pairs were not valid.'});
                })
                .then(done, done.fail);
        });
        it('should not add when not given a submission', function (done) {
            agent
                .post(path)
                .expect(400)
                .expect('Content-Type', /json/)
                .then(function (response) {
                    expect(response.body).toEqual({error: 'Pairs were not valid.'});
                })
                .then(done, done.fail);
        });
    });

    describe("DELETE", function () {
        beforeEach(function (done) {
            agent
                .post(path)
                .send(validPairs)
                .then(done, done.fail);
        });

        it('will remove a set of pair assignments.', function (done) {
            agent.delete(path + '/' + validPairs._id)
                .expect(200)
                .then(function (response) {
                    expect(response.body).toEqual({message: 'SUCCESS'});
                    return agent.get(path);
                })
                .then(function (response) {
                    let result = response.body.some(function (pairAssignments) {
                        return validPairs._id == pairAssignments._id;
                    });
                    expect(result).toBe(false);
                })
                .then(done, done.fail);
        });

        it('will return an error when specific pair assignments do not exist.', function (done) {
            setTimeout(function () {
                let badId = monk.id();
                agent.delete(path + '/' + badId)
                    .expect(404)
                    .then(function (response) {
                        expect(response.body).toEqual({message: 'Pair Assignments could not be deleted because they do not exist.'});
                    })
                    .then(done, done.fail);
            });
        });
    });
});