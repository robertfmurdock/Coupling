"use strict";
var should = require('should');
var Supertest = require('supertest');
var DataService = require('../../lib/CouplingDataService');
var PairAssignmentDocument = require('../../lib/PairAssignmentDocument');
var Comparators = require('../../lib/Comparators');
var monk = require('monk');
var config = require('../../config');

var host = 'http://localhost:' + config.port;
var tribeId = 'test';
var path = '/api/' + tribeId + '/history';

var database = monk(config.mongoUrl);
var historyCollection = database.get('history');

describe(path, function () {
    var validPairs = new PairAssignmentDocument(new Date(), [
        [
            {name: "Shaggy"},
            {name: "Scooby"}
        ]
    ]);
    validPairs._id = "mysterymachine";
    validPairs.tribe = tribeId;

    historyCollection.remove({_id: validPairs._id}, false);

    var supertest = Supertest('http://localhost:' + config.port);
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

    describe("POST will save pairs", function () {

        it('should add when given a valid pair assignment document.', function (done) {
            var post = supertest.post(path);
            post.cookies = Cookies;
            post.
                send(validPairs)
                .expect('Content-Type', /json/).
                end(function (error, response) {
                    response.status.should.equal(200);
                    var pairsAsSaved = response.body;

                    new DataService(config.mongoUrl).requestHistory(tribeId, function (history) {
                        var latestEntryInHistory = history[0];
                        for (var parameterName in pairsAsSaved) {
                            if (pairsAsSaved.hasOwnProperty(parameterName)) {
                                var actualParameterValue = latestEntryInHistory[parameterName];
                                var expectedParameterValue = pairsAsSaved[parameterName];
                                JSON.stringify(actualParameterValue).should.equal(JSON.stringify(expectedParameterValue));
                            } else {
                                should.fail;
                            }
                        }
                        done();
                    }, function (error) {
                        done(error);
                    });
                });
        });
        it('should not add when given a document without a date', function (done) {
            var pairs = { pairs: [
                [
                    {name: "Shaggy"},
                    {name: "Scooby"}
                ]
            ]};
            var post = supertest.post(path);
            post.cookies = Cookies;
            post.send(pairs)
                .expect('Content-Type', /json/).
                end(function (error, response) {
                    response.status.should.equal(400);
                    response.body.should.eql({error: 'Pairs were not valid.'});
                    done();
                });
        });
        it('should not add when given a document without pairs', function (done) {
            var pairs = { date: new Date()};
            var post = supertest.post(path);
            post.cookies = Cookies;
            post.send(pairs)
                .expect('Content-Type', /json/).
                end(function (error, response) {
                    response.status.should.equal(400);
                    response.body.should.eql({error: 'Pairs were not valid.' });
                    done();
                });
        });
        it('should not add when not given a submission', function (done) {
            var post = supertest.post(path);
            post.cookies = Cookies;
            post.expect('Content-Type', /json/).
                end(function (error, response) {
                    response.status.should.equal(400);
                    response.body.should.eql({error: 'Pairs were not valid.' });
                    done();
                });
        });
    });

    describe("DELETE", function () {
        beforeEach(function (done) {
            var post = supertest.post(path);
            post.cookies = Cookies;
            post.send(validPairs)
                .end(function (error) {
                    should.not.exist(error);
                    done();
                });
        });

        it('will remove a set of pair assignments.', function (done) {
            setTimeout(function () {
                var httpDelete = supertest.delete(path + '/' + validPairs._id);
                httpDelete.cookies = Cookies;
                httpDelete.end(function (error, response) {
                    should.not.exist(error);
                    response.status.should.equal(200);
                    response.body.should.eql({ message: 'SUCCESS' });

                    var httpGet = supertest.get(path);
                    httpGet.cookies = Cookies;
                    httpGet.end(function (error, response) {
                        var result = response.body.some(function (pairAssignments) {
                            return validPairs._id == pairAssignments._id;
                        });

                        result.should.be.false;
                        done();
                    });
                });
            });
        });

        it('will return an error when specific pair assignments do not exist.', function (done) {
            setTimeout(function () {
                var badId = "veryBadId";
                var httpDelete = supertest.delete(path + '/' + badId);
                httpDelete.cookies = Cookies;
                httpDelete.end(function (error, response) {
                    should.not.exist(error);
                    response.status.should.equal(404);
                    response.body.should.eql({ message: 'Pair Assignments could not be deleted because they do not exist.' });
                    done();
                });
            });
        });
    });
});