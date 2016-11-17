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

var database = monk(config.tempMongoUrl);
var pinCollection = database.get('pins');

var clean = function (object) {
  return JSON.parse(JSON.stringify(object));
};


describe(path, function () {

  beforeEach(function (done) {
    supertest.get('/test-login?username="name"&password="pw"')
      .expect(302)
      .end(function (err) {
        expect(err).to.not.exist;
        done();
      });
  });

  describe("GET", function () {
    var expectedPins = [
      {_id: monk.id(), tribe: tribeId},
      {_id: monk.id(), tribe: tribeId},
      {_id: monk.id(), tribe: tribeId}
    ];

    beforeEach(function (done) {
      pinCollection.remove({tribe: tribeId});
      pinCollection.insert(expectedPins, done);
    });

    afterEach(function () {
      pinCollection.remove({tribe: tribeId});
    });

    it('will return all available pins on tribe.', function (done) {
      var httpGet = supertest.get(path);
      httpGet.expect(200).expect('Content-Type', /json/).end(function (error, response) {
        expect(clean(response.body)).to.eql(clean(expectedPins));
        done(error);
      });
    });

    it('will return error when tribe is not available.', function (done) {
      var httpGet = supertest.get(badTribePath);
      httpGet.expect('Content-Type', /json/).end(function (error, response) {
        if (error) {
          done(error);
        }

        expect(response.body).to.eql([]);
        done();
      });
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
        .then(function () {
          done()
        }, done)
    });

    afterEach(function (done) {
      pinCollection.drop()
        .then(function () {
          done();
        }, done);
    });

    it("will add pin to tribe", function (done) {
      var newPin = {_id: monk.id(), tribe: tribeId};
      var httpPost = supertest.post(path);
      httpPost.send(newPin)
        .expect('Content-Type', /json/)
        .expect(200)
        .end(function (error, response) {
          if (error) {
            done(error);
          }

          var expectedPins = resultPins.concat(newPin);
          expect(clean(response.body)).to.eql(clean(newPin));

          dataService.requestPins(tribeId)
            .then(function (results) {
              expect(results).eql(expectedPins);
              done(error);
            })
            .catch(done);
        });
    });
  });
  describe("DELETE", function () {
    var resultPins = [
      {_id: monk.id(), tribe: tribeId},
      {_id: monk.id(), tribe: tribeId},
      {_id: monk.id(), tribe: tribeId}
    ];

    beforeEach(function (done) {
      pinCollection.insert(resultPins, done);
    });

    afterEach(function () {
      pinCollection.remove({tribe: tribeId});
    });

    it('will no longer display the deleted pin', function (done) {
      var httpDelete = supertest.delete(path + "/" + resultPins[1]._id);
      httpDelete
        .expect('Content-Type', /json/)
        .expect(200)
        .end(function (error, response) {
          if (error) {
            done(error);
          }

          expect(response.body).to.eql({});

          var httpGet = supertest.get(path);
          httpGet.expect(200).expect('Content-Type', /json/).end(function (error, response) {
            if (error) {
              done(error);
            } else {
              expect(clean(response.body)).to.eql(clean([resultPins[0], resultPins[2]]));
              done();
            }
          });
        });
    });

    it('will fail when pin does not exist', function (done) {
      var httpDelete = supertest.delete(path + "/" + monk.id());
      httpDelete
        .expect('Content-Type', /json/)
        .expect(404)
        .end(function (error, response) {
          if (error) {
            done(error);
          }
          expect(response.body).to.eql({message: 'Failed to remove the pin because it did not exist.'});
          done();
        });
    });
  });
});