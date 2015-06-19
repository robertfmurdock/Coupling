"use strict";
var expect = require('chai').expect;
var DataService = require('../../server/lib/CouplingDataService');
var Comparators = require('../../server/lib/Comparators');

var config = require('./../../config');
var tribeId = 'test';
var server = 'http://localhost:' + config.port;
var SupertestSession = require('supertest-session')({app: server});

var path = '/api/' + tribeId + '/players';

var monk = require('monk');
var database = monk(config.tempMongoUrl);
var playersCollection = database.get('players');


describe(path, function () {

  var couplingServer = new SupertestSession();

  beforeEach(function (done) {
    couplingServer.get('/test-login?username="name"&password="pw"')
      .expect(302)
      .end(done);
  });

  afterEach(function () {
    couplingServer.destroy();
    playersCollection.remove({tribe: tribeId}, false);
  });

  describe("GET", function () {
    it('will return all available players on team.', function (done) {
      var service = new DataService(config.tempMongoUrl);

      service.requestPlayers(tribeId).then(function (players) {
        var httpGet = couplingServer.get(path);
        httpGet.expect(200)
          .expect('Content-Type', /json/)
          .end(function (error, response) {
            expect(response.body).to.eql(players);
            done(error);
          });
      }, done);
    });
  });

  describe("POST", function () {

    it('will add player to tribe', function (done) {
      var newPlayer = {_id: 'playerOne', name: "Awesome-O", tribe: tribeId};
      var httpPost = couplingServer.post(path);
      httpPost.send(newPlayer)
        .expect(200, newPlayer)
        .end(function (error) {
          if (error) {
            done(error);
          } else {
            var httpGet = couplingServer.get(path);
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
      httpPost.send(newPlayer).end(function (error, responseContainingTheNewId) {
        newPlayer = responseContainingTheNewId.body;
        done(error);
      });
    });

    it('will remove a given player.', function (done) {
      var httpDelete = couplingServer.delete(path + "/" + newPlayer._id);
      httpDelete.expect(200, function () {
        var httpGet = couplingServer.get(path);
        httpGet.end(function (error, response) {
          var result = response.body.some(function (player) {
            return Comparators.areEqualPlayers(newPlayer, player);
          });
          expect(result).to.be.false;
          done(error);
        });
      });
    });

    it('will return an error when the player does not exist.', function (done) {
      var badId = "terribleTerribleIdentifier";
      var httpDelete = couplingServer.delete(path + "/" + badId);
      httpDelete
        .expect(404, {message: 'Failed to remove the player because it did not exist.'})
        .end(done);
    });
  });
});

