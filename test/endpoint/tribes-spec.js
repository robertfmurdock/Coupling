'use strict';
var config = require('../../config');
var server = 'http://localhost:' + config.port;

var expect = require('chai').expect;
var monk = require('monk');
var _ = require('underscore');

var path = '/api/tribes';
var host = require("supertest-as-promised").agent(server);

function clean(object) {
  return JSON.parse(JSON.stringify(object));
}

var database = monk(config.tempMongoUrl);
var tribesCollection = database.get('tribes');
var playersCollection = database.get('players');
var usersCollection = monk(config.mongoUrl).get('users');

describe(path, function () {
  var userEmail = 'test@test.tes';

  beforeEach(function (done) {
    host.get('/test-login?username=' + userEmail + '&password=pw')
      .expect(302)
      .then(function () {
        return playersCollection.drop();
      })
      .then(function () {
        return tribesCollection.drop();
      })
      .then(function () {
        done()
      }, done)
  });

  function authorizeUserForTribes(authorizedTribes) {
    usersCollection.update({email: userEmail + "._temp"}, {$set: {tribes: authorizedTribes}});
  }

  it('GET will return all available tribes.', function (done) {
    tribesCollection.find({}, {}, function (error, tribeDocuments) {
      var authorizedTribes = _.pluck(tribeDocuments, '_id');
      authorizeUserForTribes(authorizedTribes);

      host.get(path)
        .expect(200)
        .expect('Content-Type', /json/)
        .then(function (response) {
          expect(response.body).to.eql(clean(tribeDocuments));
          done();
        })
        .catch(function (err) {
          done(err);
        });
    });
  });

  it('GET will return any tribe that has a player with the given email.', function (done) {
    var tribe = {id: 'delete-me', name: 'tribe-from-endpoint-tests'};
    tribesCollection.insert(tribe);
    var playerId = monk.id();
    playersCollection.insert({_id: playerId, name: 'delete-me', tribe: 'delete-me', email: userEmail});

    authorizeUserForTribes([]);

    host.get(path)
      .expect(200)
      .expect('Content-Type', /json/)
      .then(function (response) {
        expect(clean(response.body)).to.eql(clean([tribe]));

        tribesCollection.remove({id: 'delete-me'}, false);
        playersCollection.remove({_id: playerId}, function (err) {
          done(err);
        })
      })
      .catch(function (err) {
        done(err);
      });
  });

  it('GET will not return all available tribes when the user does not have explicit permission.', function (done) {
    authorizeUserForTribes([]);
    host.get(path)
      .expect(200)
      .expect('Content-Type', /json/)
      .end(function (error, response) {
        expect(response.body).to.eql([]);
        done(error);
      });
  });

  describe('POST', function () {
    var newTribe = {name: 'TeamMadeByTest', id: 'deleteme', _id: monk.id()};

    it('will create a tribe and authorize it.', function (done) {
      host.post(path)
        .send(newTribe)
        .expect(200)
        .expect('Content-Type', /json/)
        .end(function (error, response) {
          expect(error).to.not.exist;
          expect(JSON.stringify(response.body)).to.equal(JSON.stringify(newTribe));

          host.get(path)
            .expect(200)
            .expect('Content-Type', /json/)
            .end(function (error, response) {
              expect(error).to.not.exist;
              expect(_.findWhere(response.body, clean(newTribe))).to.exist;
              done();
            });
        });
    });

    after(function () {
      tribesCollection.remove({id: newTribe.id}, false);
    });
  });
});