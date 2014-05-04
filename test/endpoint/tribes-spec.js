"use strict";
var supertest = require('supertest');
var should = require('should');
var config = require('./../../config');
var monk = require('monk');
var _ = require('underscore');

var path = '/api/tribes';
describe(path, function () {

    var host = supertest('http://localhost:3000');
    var database = monk(config.mongoUrl);
    var teamCollection = database.get('tribes');
    it('GET will return all available tribes.', function (done) {

        teamCollection.find({}, {}, function (error, tribeDocuments) {
            host.get(path).expect('Content-Type', /json/).end(function (error, response) {
                should.not.exist(error);
                response.status.should.equal(200);
                JSON.stringify(response.body).should.equal(JSON.stringify(tribeDocuments));
                done();
            });
        });
    });

    describe('POST', function () {
        var newTribe = {name: 'TeamMadeByTest', _id: 'deleteme'};

        it('will create a tribe.', function (done) {
            host.post(path).send(newTribe).expect('Content-Type', /json/).end(function (error, response) {
                should.not.exist(error);
                response.status.should.equal(200);
                JSON.stringify(response.body).should.equal(JSON.stringify(newTribe));

                host.get(path).expect('Content-Type', /json/).end(function (error, response) {
                    should.not.exist(error);
                    response.status.should.equal(200);
                    _.findWhere(response.body, newTribe).should.exist;
                    done();
                });
            });
        });

        after(function () {
            teamCollection.remove({_id: newTribe._id}, false);
        });
    });
});