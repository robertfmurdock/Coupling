"use strict";
var supertest = require('supertest');
var should = require('should');
var tribeId = 'test';
var path = '/api/' + tribeId + '/game';
describe(path, function () {

    it('will take the players given and use those for pairing.', function (done) {
        var onlyEnoughPlayersForOnePair = [
            {name: "dude1"},
            {name: "dude2"}
        ];
        supertest('http://localhost:3000').post(path).send(onlyEnoughPlayersForOnePair).expect('Content-Type', /json/).end(function (error, response) {
            response.status.should.equal(200);
            response.body.tribe.should.equal(tribeId);
            JSON.stringify(response.body.pairs).should.equal(JSON.stringify([onlyEnoughPlayersForOnePair]));
            done();
        });
    });
});