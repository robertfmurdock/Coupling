"use strict";
var supertest = require('supertest');
var should = require('should');
var CouplingDataService = require('../../lib/CouplingDataService');
var config = require('./../../config');

var path = '/api/players';
describe(path, function () {

    it('will return all available players.', function (done) {
        var service = new CouplingDataService(config.mongoUrl);

        service.requestPlayers(null, function (players) {
            supertest('http://localhost:3000').get(path).expect('Content-Type', /json/).end(function (error, response) {
                should.not.exist(error);
                response.status.should.equal(200);
                JSON.stringify(response.body).should.equal(JSON.stringify(players));
                done();
            });
        }, function (error) {
            should.not.exist(error);
            done();
        });

    });

});