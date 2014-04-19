"use strict";
var supertest = require('supertest');
var should = require('should');
var adapter = require('../../lib/CouplingDatabaseAdapter');
var config = require('./../../config');

describe('api/game', function () {

    it('will take the players given and use those for pairing.', function (done) {
        adapter(config.mongoUrl, function () {
            var onlyEnoughPlayersForOnePair = [
                {name: "dude1"},
                {name: "dude2"}
            ];
            supertest('http://localhost:3000').post('/api/game').send(onlyEnoughPlayersForOnePair).expect('Content-Type', /json/).end(function (error, response) {
                response.status.should.equal(200);
                JSON.stringify(response.body.pairs).should.equal(JSON.stringify([onlyEnoughPlayersForOnePair]));
                done();
            });
        }, function (error) {
            should.not.exist(error);
            done();
        });

    });

});