"use strict";
var config = require('../../config');
var server = 'http://localhost:' + config.port;
var supertest = require("supertest-as-promised").agent(server);
var expect = require('chai').expect;
var monk = require('monk');

var tribeId = 'test';
var pinId = monk.id();
var path = '/api/' + tribeId + '/spin';

var database = monk(config.testMongoUrl + '/CouplingTemp');
var pinCollection = database.get('pins');

describe(path, function () {

  before(function () {
    removeTestPin();
  });

  beforeEach(function (done) {
    supertest.get('/test-login?username="name"&password="pw"')
      .expect(302)
      .end(done);
  });

  function removeTestPin() {
    pinCollection.remove({tribe: tribeId});
  }

  afterEach(function () {
    removeTestPin();
  });

  var decorateWithPins = function (pair) {
    pair.forEach(function (player) {
      player.pins = []
    })
  };

  it('will take the players given and use those for pairing.', function (done) {
    var onlyEnoughPlayersForOnePair = [
      {name: "dude1"},
      {name: "dude2"}
    ];
    supertest.post(path).send(onlyEnoughPlayersForOnePair)
      .expect(200)
      .expect('Content-Type', /json/)
      .end(function (error, response) {
        expect(response.body.tribe).to.equal(tribeId);
        decorateWithPins(onlyEnoughPlayersForOnePair);
        var expectedPairAssignments = [onlyEnoughPlayersForOnePair];
        expect(response.body.pairs).to.eql(expectedPairAssignments);
        done(error);
      });
  });

  describe("when a pin exists", function () {

    var pin = {_id: pinId, tribe: tribeId, name: 'super test pin'};
    beforeEach(function (done) {
      pinCollection.insert(pin)
        .then(function () {
          done();
        });
    });

    it('will assign one pin to a player', function (done) {
      var players = [
        {name: "dude1"}
      ];
      supertest.post(path).send(players)
        .expect(200)
        .expect('Content-Type', /json/)
        .end(function (error, response) {
          expect(response.body.tribe).to.equal(tribeId);
          var expectedPinnedPlayer = {name: "dude1", pins: [pin]};
          var expectedPairAssignments = [
            [expectedPinnedPlayer]
          ];
          expect(JSON.stringify(response.body.pairs)).to.eql(JSON.stringify(expectedPairAssignments));
          done(error);
        });
    });
  });
});