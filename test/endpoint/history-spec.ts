"use strict";

import * as Promise from "bluebird";
import CouplingDataService from "../../server/lib/CouplingDataService";
import * as supertest from "supertest";
import * as monk from "monk";

let config = require('../../server/config/config');
let server = 'http://localhost:' + config.port;
let agent = supertest.agent(server);
let tribeId = 'endpointTest';
let path = '/api/' + tribeId + '/history';

let database = monk.default(config.tempMongoUrl);
let historyCollection = database.get('history');

describe(path, function () {
    let validPairs = {
        date: new Date().toISOString(),
        pairs: [
            [
                {name: "Shaggy"},
                {name: "Scooby"}
            ]
        ],
        _id: monk.id().toString(),
        tribe: tribeId
    };

    beforeAll(function (done) {
        historyCollection.remove({tribe: tribeId}, false)
            .then(done, done.fail);
    });

    beforeEach(function (done) {
        agent.get('/test-login?username="name"&password="pw"')
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
    });

    describe("POST will save pairs", function () {

        it('should add when given a valid pair assignment document.', function (done) {
            agent.post(path)
                .send(validPairs)
                .expect(200)
                .expect('Content-Type', /json/)
                .then(function (response) {
                    let pairsAsSaved = response.body;

                    let dataService = new CouplingDataService(config.tempMongoUrl);

                    return Promise.props(
                        {
                            pairsAsSaved: pairsAsSaved,
                            history: dataService.requestHistory(tribeId)
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