"use strict";
var should = require('should');
var supertest = require('supertest');
var DataService = require('../../lib/CouplingDataService');
var PairAssignmentDocument = require('../../lib/PairAssignmentDocument');
var config = require('../../config');

var host = 'http://localhost:3000';
var path = '/api/test/history';

describe(path, function () {
    describe("POST will save pairs", function () {
        var validPairs = new PairAssignmentDocument(new Date(), [
            [
                {name: "Shaggy"},
                {name: "Scooby"}
            ]
        ]);
        validPairs._id = "mysterymachine";

        afterEach(function () {
            var monk = require('monk');
            var database = monk(config.mongoUrl);
            var historyCollection = database.get('history');
            historyCollection.remove({_id: validPairs._id}, false);
        });

        it('should add when given a valid pair assignment document.', function (done) {
            supertest(host).post(path).
                send(validPairs)
                .expect('Content-Type', /json/).
                end(function (error, response) {
                    response.status.should.equal(200);
                    var pairsAsSaved = response.body;
                    new DataService(config.mongoUrl).requestHistory(null, function (history) {
                        JSON.stringify(history[0]).should.equal(JSON.stringify(pairsAsSaved));
                        done();
                    }, function (error) {
                        should.not.exist(error);
                        done();
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
            supertest(host).post(path).
                send(pairs)
                .expect('Content-Type', /json/).
                end(function (error, response) {
                    response.status.should.equal(400);
                    response.body.should.eql({error: 'Pairs were not valid.'});
                    done();
                });
        });
        it('should not add when given a document without pairs', function (done) {
            var pairs = { date: new Date()};
            supertest(host).post(path).
                send(pairs)
                .expect('Content-Type', /json/).
                end(function (error, response) {
                    response.status.should.equal(400);
                    response.body.should.eql({error: 'Pairs were not valid.' });
                    done();
                });
        });
        it('should not add when not given a submission', function (done) {
            supertest(host).post(path)
                .expect('Content-Type', /json/).
                end(function (error, response) {
                    response.status.should.equal(400);
                    response.body.should.eql({error: 'Pairs were not valid.' });
                    done();
                });
        });
    });
});
