"use strict";
var config = require('../../config');
var server = 'http://localhost:' + config.port;
var supertest = require("supertest-as-promised").agent(server);
var expect = require('chai').expect;
var monk = require('monk');
var CouplingDataService = require('../../server/lib/CouplingDataService');
var dataService = new CouplingDataService(config.tempMongoUrl);
var tribeId = 'test';
var path = '/api/' + tribeId + '/pins';
var badTribePath = '/api/does-not-exist/pins';
var Promise = require('bluebird');

var database = monk(config.tempMongoUrl);
var pinCollection = database.get('pins');

var clean = function (object) {
  return JSON.parse(JSON.stringify(object));
};

fdescribe(path, function () {

  beforeEach(function (done) {
    supertest.get('/test-login?username="name"&password="pw"')
      .expect(302)
      .then(done, done.fail);
  });

  describe("GET", function () {
    var expectedPins = [
      {_id: monk.id(), tribe: tribeId},
      {_id: monk.id(), tribe: tribeId},
      {_id: monk.id(), tribe: tribeId}
    ];

    beforeEach(function (done) {
      pinCollection.remove({tribe: tribeId})
        .then(function () {
          return pinCollection.insert(expectedPins)
        })
        .then(done, done.fail);
    });

    afterEach(function (done) {
      pinCollection.remove({tribe: tribeId})
        .then(done, done.fail);
    });

    it('will return all available pins on tribe.', function (done) {
      supertest.get(path)
        .expect(200)
        .expect('Content-Type', /json/)
        .then(function (response) {
          expect(clean(response.body)).to.eql(clean(expectedPins));
        })
        .then(done, done.fail);
    });

    it('will return error when tribe is not available.', function (done) {
      supertest.get(badTribePath)
        .expect('Content-Type', /json/)
        .then(function (response) {
          expect(response.body).to.eql([]);
        })
        .then(done, done.fail);
    });
  });

  describe("POST", function () {
    var resultPins = [
      {_id: monk.id(), tribe: tribeId},
      {_id: monk.id(), tribe: tribeId},
      {_id: monk.id(), tribe: tribeId}
    ];

    beforeEach(function (done) {
      pinCollection.drop()
        .then(function () {
          return pinCollection.insert(resultPins);
        })
        .then(done, done.fail);
    });

    afterEach(function (done) {
      pinCollection.drop()
        .then(done, done.fail);
    });

    it("will add pin to tribe", function (done) {
      var newPin = {_id: monk.id(), tribe: tribeId};
      var httpPost = supertest.post(path);
      httpPost.send(newPin)
        .expect('Content-Type', /json/)
        .expect(200)
        .then(function (response) {
          var expectedPins = resultPins.concat(newPin);
          expect(clean(response.body)).to.eql(clean(newPin));

          return Promise.props({
            expectedPins: expectedPins,
            results: dataService.requestPins(tribeId)
          });
        })
        .then(function (props) {
          var expectedPins = props.expectedPins;
          expect(props.results).eql(expectedPins);
        })
        .then(done, done.fail);
    });
  });
  describe("DELETE", function () {
    var resultPins = [
      {_id: monk.id(), tribe: tribeId},
      {_id: monk.id(), tribe: tribeId},
      {_id: monk.id(), tribe: tribeId}
    ];

    beforeEach(function (done) {
      pinCollection.insert(resultPins)
        .then(done, done.fail);
    });

    afterEach(function (done) {
      pinCollection.remove({tribe: tribeId})
        .then(done, done.fail);
    });

    it('will no longer display the deleted pin', function (done) {
      var httpDelete = supertest.delete(path + "/" + resultPins[1]._id);
      httpDelete
        .expect('Content-Type', /json/)
        .expect(200)
        .then(function (response) {
          expect(response.body).to.eql({});

          return supertest.get(path)
            .expect(200)
            .expect('Content-Type', /json/)
        })
        .then(function (response) {
          expect(clean(response.body)).to.eql(clean([resultPins[0], resultPins[2]]));
        })
        .then(done, done.fail);
    });

    it('will fail when pin does not exist', function (done) {
      var httpDelete = supertest.delete(path + "/" + monk.id());
      httpDelete
        .expect('Content-Type', /json/)
        .expect(404)
        .then(function (response) {
          expect(response.body).to.eql({message: 'Failed to remove the pin because it did not exist.'});
        })
        .then(done, done.fail);
    });
  });
});