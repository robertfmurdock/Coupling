'use strict';
import * as supertest from "supertest-as-promised";
import * as Promise from "bluebird";
import * as monk from "monk";
import * as _ from "underscore";

let config = require('../../config');
let server = 'http://localhost:' + config.port;

let path = '/api/tribes';
let host = supertest.agent(server);

function clean(object) {
    return JSON.parse(JSON.stringify(object));
}

let database = monk(config.tempMongoUrl);
let tribesCollection = database.get('tribes');
let playersCollection = database.get('players');
let usersCollection = monk(config.mongoUrl).get('users');

describe(path, function () {
    let userEmail = 'test@test.tes';

    beforeEach(function (done) {
        host.get('/test-login?username=' + userEmail + '&password=pw')
            .expect(302)
            .then(() => Promise.all([
                playersCollection.drop(),
                tribesCollection.drop()
            ]))
            .then(done, done.fail);
    });

    function authorizeUserForTribes(authorizedTribes) {
        return usersCollection.update({email: userEmail + "._temp"}, {$set: {tribes: authorizedTribes}});
    }

    it('GET will return all available tribes.', function (done) {
        tribesCollection.find({}, {})
            .then(tribeDocuments => {
                let authorizedTribes = _.pluck(tribeDocuments, '_id');
                return authorizeUserForTribes(authorizedTribes)
                    .then(() => tribeDocuments);
            })
            .then(tribeDocuments => {
                return host.get(path)
                    .expect(200)
                    .expect('Content-Type', /json/)
                    .then(function (response) {
                        expect(response.body).toEqual(clean(tribeDocuments));
                    })
            }).then(done, done.fail);
    });

    it('GET will return any tribe that has a player with the given email.', function (done) {
        let tribe = {id: 'delete-me', name: 'tribe-from-endpoint-tests'};
        let playerId = monk.id();
        Promise.all([
            tribesCollection.insert(tribe),
            playersCollection.insert({_id: playerId, name: 'delete-me', tribe: 'delete-me', email: userEmail}),
            authorizeUserForTribes([])
        ])
            .then(function () {
                return host.get(path)
                    .expect(200)
                    .expect('Content-Type', /json/)
            })
            .then(function (response) {
                expect(clean(response.body)).toEqual(clean([tribe]));
                return Promise.all([
                    tribesCollection.remove({id: 'delete-me'}, false),
                    playersCollection.remove({_id: playerId})
                ])
            })
            .then(done, done.fail);
    });

    it('GET will not return all available tribes when the user does not have explicit permission.', function (done) {
        authorizeUserForTribes([])
            .then(() => {
                return host.get(path)
                    .expect(200)
                    .expect('Content-Type', /json/)
            })
            .then(function (response) {
                expect(response.body).toEqual([]);
            })
            .then(done, done.fail);
    });

    describe('POST', function () {
        let newTribe = {name: 'TeamMadeByTest', id: 'deleteme', _id: monk.id()};

        it('will create a tribe and authorize it.', function (done) {
            host.post(path)
                .send(newTribe)
                .expect(200)
                .expect('Content-Type', /json/)
                .then(function (response) {
                    expect(JSON.stringify(response.body)).toEqual(JSON.stringify(newTribe));

                    return host.get(path)
                        .expect(200)
                        .expect('Content-Type', /json/)
                })
                .then(function (response) {
                    expect(_.findWhere(response.body, clean(newTribe))).toBeDefined();
                })
                .then(done, done.fail);
        });

        afterAll(function (done) {
            tribesCollection.remove({id: newTribe.id}, false)
                .then(done, done.fail);
        });
    });
});