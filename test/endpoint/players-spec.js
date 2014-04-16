"use strict";
var supertest = require('supertest');
require('should');
var adapter = require('../../lib/CouplingDatabaseAdapter');
var databaseUrl = 'localhost:27017/Coupling';

describe('Routing', function () {

    it('players', function (done) {

        adapter(databaseUrl, function (players) {
            supertest('http://localhost:3000').get('/players').expect('Content-Type', /json/).end(function (error, response) {
                response.status.should.equal(200);
                JSON.stringify(response.body).should.equal(JSON.stringify(players));
                done();
            });
        });

    });

});