"use strict";
var supertest = require('supertest');
var should = require('should');
var DataService = require('../../lib/CouplingDataService');
var Comparators = require('../../lib/Comparators');

var config = require('./../../config');
var tribeId = 'test';
var server = 'http://localhost:3000';
var couplingServer = supertest(server);
var path = '/api/' + tribeId + '/players';

var monk = require('monk');
var database = monk(config.mongoUrl);
var playersCollection = database.get('players');


describe(path, function () {
    describe("GET", function () {
        it('will return all available players on team.', function (done) {
            var service = new DataService(config.mongoUrl);

            service.requestPlayers(tribeId, function (players) {
                couplingServer.get(path).expect('Content-Type', /json/).end(function (error, response) {
                    should.not.exist(error);
                    response.status.should.equal(200);
                    JSON.stringify(response.body).should.equal(JSON.stringify(players));
                    done();
                });
            }, function (error) {
                should.not.exist(error);
                done();
            });
        });
    });

    describe("POST", function () {
    });

    describe("DELETE", function () {
        var newPlayer = {name: "Awesome-O", tribe: tribeId};
        beforeEach(function (done) {
            couplingServer.post(path).send(newPlayer).end(function (error, responseContainingTheNewId) {
                should.not.exist(error);
                newPlayer = responseContainingTheNewId.body;
                done();
            });
        });

        afterEach(function () {
            playersCollection.remove({_id: newPlayer._id}, false);
        });

        it('will remove a given player.', function (done) {
            couplingServer.delete(path + "/" + newPlayer._id).end(function (error, response) {
                should.not.exist(error);
                response.status.should.equal(200);

                supertest(server).get(path).end(function (error, response) {
                    var result = response.body.some(function (player) {
                        return Comparators.areEqualPlayers(newPlayer, player);
                    });
                    result.should.be.false;
                    done();
                });
            });
        });

        it('will return an error when the player does not exist.', function (done) {
            var badId = "terribleTerribleIdentifier";
            couplingServer.delete(path + "/" + badId).end(function (error, response) {
                should.not.exist(error);
                response.status.should.equal(404);
                response.body.should.eql({message: 'Failed to remove the player because it did not exist.'});
                done();
            });
        });
    });
});

