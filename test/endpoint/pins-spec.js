"use strict";
var Supertest = require('supertest');
var expect = require('chai').expect;
var config = require('../../config');
var monk = require('monk');
var CouplingDataService = require('../../lib/CouplingDataService');
var dataService = new CouplingDataService(config.tempMongoUrl);
var tribeId = 'test';
var path = '/api/' + tribeId + '/pins';
var badTribePath = '/api/does-not-exist/pins';
var host = 'http://localhost:' + config.port;

var database = monk(config.tempMongoUrl);
var pinCollection = database.get('pins');

describe(path, function () {
    var supertest = Supertest(host);
    var Cookies;

    beforeEach(function (done) {
        supertest.get('/test-login?username="name"&password="pw"').end(function (err, res) {
            expect(err).to.not.exist;
            Cookies = res.headers['set-cookie'].pop().split(';')[0];
            done();
        });
    });

    describe("GET", function () {
        var expectedPins = [
            {_id: 'pin1', tribe: tribeId},
            {_id: 'pin2', tribe: tribeId},
            {_id: 'pin3', tribe: tribeId}
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
            httpGet.cookies = Cookies;
            httpGet.expect('Content-Type', /json/).end(function (error, response) {
                if (error) {
                    done(error);
                }
                response.status.should.equal(200);
                JSON.stringify(response.body).should.equal(JSON.stringify(expectedPins));
                done();
            });
        });

        it('will return error when tribe is not available.', function (done) {
            var httpGet = supertest.get(badTribePath);
            httpGet.cookies = Cookies;
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
            {_id: 'pin1', tribe: tribeId},
            {_id: 'pin2', tribe: tribeId},
            {_id: 'pin3', tribe: tribeId}
        ];

        beforeEach(function (done) {
            pinCollection.remove({tribe: tribeId});
            pinCollection.insert(resultPins, done);
        });

        afterEach(function () {
            pinCollection.remove({tribe: tribeId});
        });

        it("will add pin to tribe", function (done) {
            var newPin = {_id: 'pin4', tribe: tribeId};
            var httpPost = supertest.post(path);
            httpPost.cookies = Cookies;
            httpPost.send(newPin)
                .expect('Content-Type', /json/)
                .expect(200)
                .end(function (error, response) {
                    if (error) {
                        done(error);
                    }

                    var expectedPins = resultPins.concat(newPin);
                    expect(response.body).to.eql(newPin);

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
            {_id: 'pin1', tribe: tribeId},
            {_id: 'pin2', tribe: tribeId},
            {_id: 'pin3', tribe: tribeId}
        ];

        beforeEach(function (done) {
            pinCollection.insert(resultPins, done);
        });

        afterEach(function () {
            pinCollection.remove({tribe: tribeId});
        });

        it('will no longer display the deleted pin', function (done) {
            var httpDelete = supertest.delete(path + "/" + resultPins[1]._id);
            httpDelete.cookies = Cookies;
            httpDelete
                .expect('Content-Type', /json/)
                .expect(200)
                .end(function (error, response) {
                    if (error) {
                        done(error);
                    }

                    expect(response.body).to.eql({});

                    var httpGet = supertest.get(path);
                    httpGet.cookies = Cookies;
                    httpGet.expect('Content-Type', /json/).end(function (error, response) {
                        if (error) {
                            done(error);
                        }
                        response.status.should.equal(200);
                        JSON.stringify(response.body).should.equal(JSON.stringify([resultPins[0], resultPins[2]]));
                        done();
                    });
                });
        });

        it('will fail when pin does not exist', function (done) {
            var httpDelete = supertest.delete(path + "/imaginary");
            httpDelete.cookies = Cookies;
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