"use strict";

var expect = require('chai').expect;
var config = require('../../config');
var server = 'http://localhost:' + config.port;
var supertest = require("supertest-as-promised").agent(server);
var DataService = require('../../server/lib/CouplingDataService').default;
var Comparators = require('../../server/lib/Comparators');
var monk = require('monk');
var Promise = require('bluebird');

var tribeId = 'test';
var path = '/api/' + tribeId + '/history';

var database = monk(config.tempMongoUrl);
var historyCollection = database.get('history');

fdescribe(path, function () {
  var validPairs = {
    date: new Date().toISOString(),
    pairs: [
      [
        {name: "Shaggy"},
        {name: "Scooby"}
      ]
    ]
  };
  validPairs._id = monk.id().toString();
  validPairs.tribe = tribeId;

  historyCollection.remove({tribe: tribeId}, false);

  beforeEach(function (done) {
    supertest.get('/test-login?username="name"&password="pw"')
      .expect(302)
      .then(done, done.fail);
  });

  afterEach(function () {
    historyCollection.remove({_id: validPairs._id}, false);
  });

  describe('GET', function () {

    beforeEach(function (done) {
      supertest.post(path)
        .send(validPairs)
        .expect('Content-Type', /json/)
        .then(done, done.fail);
    });

    it('will show history of tribe that has history.', function (done) {
      supertest.get(path)
        .expect('Content-Type', /json/)
        .then(function (response) {
          expect(response.body).to.eql([validPairs]);
        })
        .then(done, done.fail);
    });

    it('will show history of tribe that has no history.', function (done) {
      supertest.get('/api/test2/history')
        .expect('Content-Type', /json/)
        .then(function (response) {
          expect([]).to.eql(response.body);
        })
        .then(done, done.fail);
    });
  });

  describe("POST will save pairs", function () {

    it('should add when given a valid pair assignment document.', function (done) {
      supertest.post(path)
        .send(validPairs)
        .expect(200)
        .expect('Content-Type', /json/)
        .then(function (response) {
          var pairsAsSaved = response.body;

          var dataService = new DataService(config.tempMongoUrl);

          return Promise.props(
            {
              pairsAsSaved: pairsAsSaved,
              history: dataService.requestHistory(tribeId)
            }
          );
        })
        .then(function (props) {
          var pairsAsSaved = props.pairsAsSaved;
          var history = props.history;
          var latestEntryInHistory = history[0];

          expect(JSON.parse(JSON.stringify(pairsAsSaved)))
            .to.eql(JSON.parse(JSON.stringify(latestEntryInHistory)));
        })
        .then(done, done.fail);
    });
    it('should not add when given a document without a date', function (done) {
      var pairs = {
        pairs: [
          [
            {name: "Shaggy"},
            {name: "Scooby"}
          ]
        ]
      };
      supertest.post(path).send(pairs)
        .expect(400)
        .expect('Content-Type', /json/)
        .then(function (response) {
          expect(response.body).to.eql({error: 'Pairs were not valid.'});
        })
        .then(done, done.fail);
    });
    it('should not add when given a document without pairs', function (done) {
      var pairs = {date: new Date()};
      supertest
        .post(path)
        .send(pairs)
        .expect(400)
        .expect('Content-Type', /json/)
        .then(function (response) {
          expect(response.body).to.eql({error: 'Pairs were not valid.'});
        })
        .then(done, done.fail);
    });
    it('should not add when not given a submission', function (done) {
      supertest
        .post(path)
        .expect(400)
        .expect('Content-Type', /json/)
        .then(function (response) {
          expect(response.body).to.eql({error: 'Pairs were not valid.'});
        })
        .then(done, done.fail);
    });
  });

  describe("DELETE", function () {
    beforeEach(function (done) {
      supertest
        .post(path)
        .send(validPairs)
        .then(done, done.fail);
    });

    it('will remove a set of pair assignments.', function (done) {
      supertest.delete(path + '/' + validPairs._id)
        .expect(200)
        .then(function (response) {
          expect(response.body).to.eql({message: 'SUCCESS'});
          return supertest.get(path);
        })
        .then(function (response) {
          var result = response.body.some(function (pairAssignments) {
            return validPairs._id == pairAssignments._id;
          });
          expect(result).to.be.false;
        })
        .then(done, done.fail);
    });

    it('will return an error when specific pair assignments do not exist.', function (done) {
      setTimeout(function () {
        var badId = monk.id();
        supertest.delete(path + '/' + badId)
          .expect(404)
          .then(function (response) {
            expect(response.body).to.eql({message: 'Pair Assignments could not be deleted because they do not exist.'});
          })
          .then(done, done.fail);
      });
    });
  });
});