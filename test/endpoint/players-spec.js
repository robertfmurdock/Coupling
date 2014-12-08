"use strict";
var Supertest = require('supertest');
var should = require('should');
var expect = require('chai').expect;
var DataService = require('../../server/lib/CouplingDataService');
var Comparators = require('../../server/lib/Comparators');

var config = require('./../../config');
var tribeId = 'test';
var server = 'http://localhost:' + config.port;
var path = '/api/' + tribeId + '/players';

var monk = require('monk');
var database = monk(config.tempMongoUrl);
var playersCollection = database.get('players');


describe(path, function () {

    var couplingServer = Supertest(server);
    var Cookies;

    beforeEach(function (done) {
        couplingServer.get('/test-login?username="name"&password="pw"').end(function (err, res) {
            Cookies = res.headers['set-cookie'].pop().split(';')[0];
            done(err);
        });
    });

    afterEach(function () {
        playersCollection.remove({tribe: tribeId}, false);
    });

    describe("GET", function () {
        it('will return all available players on team.', function (done) {
            var service = new DataService(config.tempMongoUrl);

            service.requestPlayers(tribeId).then(function (players) {
                var httpGet = couplingServer.get(path);
                httpGet.cookies = Cookies;
                httpGet.expect(200)
                    .expect('Content-Type', /json/)
                    .end(function (error, response) {
                        JSON.stringify(response.body).should.equal(JSON.stringify(players));
                        done(error);
                    });
            }, done);
        });
    });

    describe("POST", function () {

        it('will add player to tribe', function (done) {
            var newPlayer = {_id: 'playerOne', name: "Awesome-O", tribe: tribeId};
            var httpPost = couplingServer.post(path);
            httpPost.cookies = Cookies;
            httpPost.send(newPlayer)
                .expect(200, newPlayer)
                .end(function (error) {
                    if (error) {
                        done(error);
                    } else {
                        var httpGet = couplingServer.get(path);
                        httpGet.cookies = Cookies;
                        httpGet
                            .expect('Content-Type', /json/)
                            .expect(200, function (error, response) {
                                expect(response.body).to.eql([newPlayer]);
                                done(error);
                            });
                    }
                });
        });
    });

    describe("DELETE", function () {
        var newPlayer = {_id: 'playerOne', name: "Awesome-O", tribe: tribeId};
        beforeEach(function (done) {
            var httpPost = couplingServer.post(path);
            httpPost.cookies = Cookies;
            httpPost.send(newPlayer).end(function (error, responseContainingTheNewId) {
                newPlayer = responseContainingTheNewId.body;
                done(error);
            });
        });

        it('will remove a given player.', function (done) {
            var httpDelete = couplingServer.delete(path + "/" + newPlayer._id);
            httpDelete.cookies = Cookies;
            httpDelete.expect(200, function () {
                var httpGet = couplingServer.get(path);
                httpGet.cookies = Cookies;
                httpGet.end(function (error, response) {
                    var result = response.body.some(function (player) {
                        return Comparators.areEqualPlayers(newPlayer, player);
                    });
                    result.should.be.false;
                    done(error);
                });
            });
        });

        it('will return an error when the player does not exist.', function (done) {
            var badId = "terribleTerribleIdentifier";
            var httpDelete = couplingServer.delete(path + "/" + badId);
            httpDelete.cookies = Cookies;
            httpDelete
                .expect(404, {message: 'Failed to remove the player because it did not exist.'})
                .end(done);
        });
    });
});

