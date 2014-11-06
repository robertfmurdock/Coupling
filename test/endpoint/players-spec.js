"use strict";
var Supertest = require('supertest');
var should = require('should');
var expect = require('chai').expect;
var DataService = require('../../lib/CouplingDataService');
var Comparators = require('../../lib/Comparators');

var config = require('./../../config');
var tribeId = 'test';
var server = 'http://localhost:' + config.port;
var path = '/api/' + tribeId + '/players';

var monk = require('monk');
var database = monk(config.mongoUrl);
var playersCollection = database.get('players');


describe(path, function () {

    var couplingServer = Supertest(server);
    var Cookies;

    beforeEach(function (done) {
        couplingServer.get('/test-login?username="name"&password="pw"').end(function (err, res) {
            Cookies = res.headers['set-cookie'].pop().split(';')[0];
            done();
        });
    });

    afterEach(function () {
        playersCollection.remove({tribe: tribeId}, false);
    });

    describe("GET", function () {
        it('will return all available players on team.', function (done) {
            var service = new DataService(config.mongoUrl);

            service.requestPlayers(tribeId).then(function (players) {
                var httpGet = couplingServer.get(path);
                httpGet.cookies = Cookies;
                httpGet.expect('Content-Type', /json/).end(function (error, response) {
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

        it('will add player to tribe', function(done){
            var newPlayer = {_id: 'playerOne', name: "Awesome-O", tribe: tribeId};
            var httpPost = couplingServer.post(path);
            httpPost.cookies = Cookies;
            httpPost.send(newPlayer).end(function (error, responseContainingTheNewId) {
                should.not.exist(error);
                expect(responseContainingTheNewId.body).to.eql(newPlayer);

                var httpGet = couplingServer.get(path);
                httpGet.cookies = Cookies;
                httpGet.expect('Content-Type', /json/).end(function (error, response) {
                    should.not.exist(error);
                    response.status.should.equal(200);
                    expect(response.body).to.eql([newPlayer]);
                    done();
                });
            });
        });
    });

    describe("DELETE", function () {
        var newPlayer = {_id: 'playerOne', name: "Awesome-O", tribe: tribeId};
        beforeEach(function (done) {
            var httpPost = couplingServer.post(path);
            httpPost.cookies = Cookies;
            httpPost.send(newPlayer).end(function (error, responseContainingTheNewId) {
                should.not.exist(error);
                newPlayer = responseContainingTheNewId.body;
                done();
            });
        });

        it('will remove a given player.', function (done) {
            var httpDelete = couplingServer.delete(path + "/" + newPlayer._id);
            httpDelete.cookies = Cookies;
            httpDelete.end(function (error, response) {
                should.not.exist(error);
                response.status.should.equal(200);

                var httpGet = couplingServer.get(path);
                httpGet.cookies = Cookies;
                httpGet.end(function (error, response) {
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
            var httpDelete = couplingServer.delete(path + "/" + badId);
            httpDelete.cookies = Cookies;
            httpDelete.end(function (error, response) {
                should.not.exist(error);
                response.status.should.equal(404);
                response.body.should.eql({message: 'Failed to remove the player because it did not exist.'});
                done();
            });
        });
    });
});

