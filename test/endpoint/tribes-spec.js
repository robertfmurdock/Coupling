"use strict";
var supertest = require('supertest');
var should = require('should');
var config = require('./../../config');
var monk = require('monk');

var path = '/api/tribes';
describe(path, function () {

    it('will return all available tribes.', function (done) {
        var database = monk(config.mongoUrl);
        var teamCollection = database.get('tribes');
        teamCollection.find({}, {}, function (error, tribeDocuments) {
            supertest('http://localhost:3000').get(path).expect('Content-Type', /json/).end(function (error, response) {
                should.not.exist(error);
                response.status.should.equal(200);
                JSON.stringify(response.body).should.equal(JSON.stringify(tribeDocuments));
                done();
            });
        });
    });
});