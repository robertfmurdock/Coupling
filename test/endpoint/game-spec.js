"use strict";
var Supertest = require('supertest');
var should = require('should');
var config = require('../../config');
var tribeId = 'test';
var path = '/api/' + tribeId + '/spin';

describe(path, function () {
    var supertest = Supertest('http://localhost:' + config.port);
    var Cookies;

    beforeEach(function (done) {
        supertest.get('/test-login?username="name"&password="pw"').end(function (err, res) {
            should.not.exist(err)
            Cookies = res.headers['set-cookie'].pop().split(';')[0];
            done();
        });
    });

    it('will take the players given and use those for pairing.', function (done) {
        var onlyEnoughPlayersForOnePair = [
            {name: "dude1"},
            {name: "dude2"}
        ];
        var post = supertest.post(path);
        post.cookies = Cookies;
        post.send(onlyEnoughPlayersForOnePair)
            .expect('Content-Type', /json/)
            .end(function (error, response) {
            should.not.exist(error);
            response.status.should.equal(200);
            response.body.tribe.should.equal(tribeId);
            JSON.stringify(response.body.pairs).should.equal(JSON.stringify([onlyEnoughPlayersForOnePair]));
            done();
        });
    });
});