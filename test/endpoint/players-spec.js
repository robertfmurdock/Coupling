"use strict";
var expect = require('chai').expect;
var DataService = require('../../server/lib/CouplingDataService').default;
var Comparators = require('../../server/lib/Comparators');

var config = require('./../../config');
var tribeId = 'test';
var server = 'http://localhost:' + config.port;
var supertest = require("supertest-as-promised").agent(server);
var Promise = require('bluebird');
var path = '/api/' + tribeId + '/players';

var monk = require('monk');
var database = monk(config.tempMongoUrl);
var playersCollection = database.get('players');

function clean(object) {
  return JSON.parse(JSON.stringify(object));
}

fdescribe(path, function () {

  var couplingServer = supertest;

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
      var service = new DataService(config.tempMongoUrl);

      Promise.props({
        expected: service.requestPlayers(tribeId),
        response: couplingServer.get(path)
          .expect(200)
          .expect('Content-Type', /json/)
      })
        .then(function (props) {
          expect(props.response.body).to.eql(props.expected);
        })
        .then(done, done.fail);
    });
  });

  describe("POST", function () {

    it('will add player to tribe', function (done) {
      var newPlayer = clean({_id: monk.id(), name: "Awesome-O", tribe: tribeId});
      var httpPost = couplingServer.post(path);
      httpPost.send(newPlayer)
        .expect(200, newPlayer)
        .then(function () {
          return couplingServer.get(path)
            .expect('Content-Type', /json/)
            .expect(200)
        })
        .then(function (response) {
          expect(response.body).to.eql([newPlayer]);
        })
        .then(done, done.fail);
    });
  });

  describe("DELETE", function () {

    var newPlayer = {_id: monk.id(), name: "Awesome-O", tribe: tribeId};

    beforeEach(function (done) {
      couplingServer.post(path)
        .send(newPlayer)
        .then(function (responseContainingTheNewId) {
          newPlayer = responseContainingTheNewId.body;
        })
        .then(done, done.fail);
    });

    it('will remove a given player.', function (done) {
      var httpDelete = couplingServer.delete(path + "/" + newPlayer._id);
      httpDelete.expect(200, function () {
        couplingServer.get(path)
          .then(function (response) {
            var result = response.body.some(function (player) {
              return Comparators.areEqualPlayers(newPlayer, player);
            });
            expect(result).to.be.false;
          })
          .then(done, done.fail);
      });
    });

    it('will return an error when the player does not exist.', function (done) {
      var badId = monk.id();
      var httpDelete = couplingServer.delete(path + "/" + badId);
      httpDelete
        .expect(404, {message: 'Failed to remove the player because it did not exist.'})
        .then(done, done.fail);
    });
  });
});

