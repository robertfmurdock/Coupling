"use strict";

var expect = require('chai').expect;
var Supertest = require('supertest');
var DataService = require('../../server/lib/CouplingDataService');
var Comparators = require('../../server/lib/Comparators');
var monk = require('monk');
var config = require('../../config');

var host = 'http://localhost:' + config.port;
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

    var supertest = Supertest(host);
    var Cookies;

    beforeEach(function (done) {
        supertest.get('/test-login?username="name"&password="pw"').end(function (err, res) {
            Cookies = res.headers['set-cookie'].pop().split(';')[0];
            done();
        });
    });

    afterEach(function () {
        historyCollection.remove({_id: validPairs._id}, false);
    });

    describe('GET', function () {

        beforeEach(function (done) {
            var post = supertest.post(path);
            post.cookies = Cookies;
            post.send(validPairs)
                .expect('Content-Type', /json/).
                end(function (error) {
                    done(error);
                });
        });

        it('will show history of tribe that has history.', function (done) {
            var httpGet = supertest.get(path);
            httpGet.cookies = Cookies;
            httpGet
                .expect('Content-Type', /json/)
                .end(function (error, response) {
                    expect([validPairs]).to.eql(response.body);
                    done();
                });
        });

        it('will show history of tribe that has no history.', function (done) {
            var httpGet = supertest.get('/api/test2/history');
            httpGet.cookies = Cookies;
            httpGet
                .expect('Content-Type', /json/)
                .end(function (error, response) {
                    expect([]).to.eql(response.body);
                    done(error);
                });
        });
    });

    describe("POST will save pairs", function () {

        it('should add when given a valid pair assignment document.', function (done) {
            var post = supertest.post(path);
            post.cookies = Cookies;
            post.
                send(validPairs)
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
            var post = supertest.post(path);
            post.cookies = Cookies;
            post.send(pairs)
                .expect(400)
                .expect('Content-Type', /json/).
                end(function (error, response) {
                    expect(response.body).to.eql({error: 'Pairs were not valid.'});
                    done(error);
                });
        });
        it('should not add when given a document without pairs', function (done) {
            var pairs = {date: new Date()};
            var post = supertest.post(path);
            post.cookies = Cookies;
            post.send(pairs).expect(400)
                .expect('Content-Type', /json/).
                end(function (error, response) {
                    expect(response.body).to.eql({error: 'Pairs were not valid.'});
                    done(error);
                });
        });
        it('should not add when not given a submission', function (done) {
            var post = supertest.post(path);
            post.cookies = Cookies;
            post.expect(400).expect('Content-Type', /json/).
                end(function (error, response) {
                    expect(response.body).to.eql({error: 'Pairs were not valid.'});
                    done(error);
                });
        });
    });

    describe("DELETE", function () {
        beforeEach(function (done) {
            var post = supertest.post(path);
            post.cookies = Cookies;
            post.send(validPairs)
                .end(function (error) {
                    done(error);
                });
        });

        it('will remove a set of pair assignments.', function (done) {
            setTimeout(function () {
                var httpDelete = supertest.delete(path + '/' + validPairs._id);
                httpDelete.cookies = Cookies;
                httpDelete.expect(200).end(function (error, response) {
                    expect(response.body).to.eql({message: 'SUCCESS'});

                    var httpGet = supertest.get(path);
                    httpGet.cookies = Cookies;
                    httpGet.end(function (error2, response) {
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
                var httpDelete = supertest.delete(path + '/' + badId);
                httpDelete.cookies = Cookies;
                httpDelete.expect(404).end(function (error, response) {
                    expect(response.body).to.eql({message: 'Pair Assignments could not be deleted because they do not exist.'});
                    done(error);
                });
            });
        });
    });
});