"use strict";

var expect = require('chai').expect;
var config = require('../../config');
var server = 'http://localhost:' + config.port;
var supertest = require("supertest-as-promised").agent(server);
var DataService = require('../../server/lib/CouplingDataService');
var Comparators = require('../../server/lib/Comparators');
var monk = require('monk');

var tribeId = 'test';
var path = '/api/' + tribeId + '/history';

var database = monk(config.tempMongoUrl);
var historyCollection = database.get('history');

describe(path, function () {
  var validPairs = {
    date: new Date().toISOString(),
    pairs: [
      [
        {name: "Shaggy"},
        {name: "Scooby"}
      ]
    ]
  };
  validPairs._id = "mysterymachine";
  validPairs.tribe = tribeId;

  historyCollection.remove({_id: validPairs._id}, false);

  beforeEach(function (done) {
    supertest.get('/test-login?username="name"&password="pw"')
      .expect(302)
      .end(done);
  });

  afterEach(function () {
    historyCollection.remove({_id: validPairs._id}, false);
  });

  describe('GET', function () {

    beforeEach(function (done) {
      supertest.post(path).send(validPairs)
        .expect('Content-Type', /json/).
        end(function (error) {
          done(error);
        });
    });

    it('will show history of tribe that has history.', function (done) {
      supertest.get(path)
        .expect('Content-Type', /json/)
        .end(function (error, response) {
          expect([validPairs]).to.eql(response.body);
          done();
        });
    });

    it('will show history of tribe that has no history.', function (done) {
      supertest.get('/api/test2/history')
        .expect('Content-Type', /json/)
        .end(function (error, response) {
          expect([]).to.eql(response.body);
          done(error);
        });
    });
  });

  describe("POST will save pairs", function () {

    it('should add when given a valid pair assignment document.', function (done) {
      supertest.post(path)
        .send(validPairs)
        .expect(200)
        .expect('Content-Type', /json/).
        end(function (error, response) {
          var pairsAsSaved = response.body;

          new DataService(config.tempMongoUrl).requestHistory(tribeId).then(function (history) {
            var latestEntryInHistory = history[0];
            for (var parameterName in pairsAsSaved) {
              if (pairsAsSaved.hasOwnProperty(parameterName)) {
                var actualParameterValue = latestEntryInHistory[parameterName];
                var expectedParameterValue = pairsAsSaved[parameterName];
                if (actualParameterValue instanceof Date) {
                  expect(actualParameterValue.toISOString()).to.eql(expectedParameterValue);
                } else {
                  expect(actualParameterValue).to.eql(expectedParameterValue);
                }

              } else {
                done("This should not be hit");
              }
            }
            done();
          }).catch(function (error) {
            done(error);
          });
        });
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
        .expect('Content-Type', /json/).
        end(function (error, response) {
          expect(response.body).to.eql({error: 'Pairs were not valid.'});
          done(error);
        });
    });
    it('should not add when given a document without pairs', function (done) {
      var pairs = {date: new Date()};
      supertest.post(path).send(pairs).expect(400)
        .expect('Content-Type', /json/).
        end(function (error, response) {
          expect(response.body).to.eql({error: 'Pairs were not valid.'});
          done(error);
        });
    });
    it('should not add when not given a submission', function (done) {
      supertest.post(path).expect(400).expect('Content-Type', /json/).
        end(function (error, response) {
          expect(response.body).to.eql({error: 'Pairs were not valid.'});
          done(error);
        });
    });
  });

  describe("DELETE", function () {
    beforeEach(function (done) {
      supertest.post(path).send(validPairs)
        .end(function (error) {
          done(error);
        });
    });

    it('will remove a set of pair assignments.', function (done) {
      setTimeout(function () {
        supertest.delete(path + '/' + validPairs._id)
          .expect(200)
          .end(function (error, response) {
            expect(response.body).to.eql({message: 'SUCCESS'});

            supertest.get(path)
              .end(function (error2, response) {
                error = error || error2;
                var result = response.body.some(function (pairAssignments) {
                  return validPairs._id == pairAssignments._id;
                });

                expect(result).to.be.false;
                done(error);
              });
          });
      });
    });

    it('will return an error when specific pair assignments do not exist.', function (done) {
      setTimeout(function () {
        var badId = "veryBadId";
        supertest.delete(path + '/' + badId)
          .expect(404)
          .end(function (error, response) {
            expect(response.body).to.eql({message: 'Pair Assignments could not be deleted because they do not exist.'});
            done(error);
          });
      });
    });
  });
});