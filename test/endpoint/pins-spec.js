"use strict";
var Supertest = require('supertest');
var expect = require('chai').expect;
var config = require('../../config');
var monk = require('monk');

var tribeId = 'test';
var path = '/api/' + tribeId + '/pins';
var badTribePath = '/api/does-not-exist/pins';
var host = 'http://localhost:' + config.port;

var database = monk(config.mongoUrl);
var pinCollection = database.get('pins');

describe(path, function () {
    var supertest = Supertest(host);
    var Cookies;

    beforeEach(function (done) {
        supertest.get('/test-login?username="name"&password="pw"').end(function (err, res) {
            expect(err).to.not.exist;
            Cookies = res.headers['set-cookie'].pop().split(';')[0];
            done();
        });
    });

    describe("GET", function () {
        var expectedPins = [
            {_id: 'pin1', tribe: tribeId},
            {_id: 'pin2', tribe: tribeId},
            {_id: 'pin3', tribe: tribeId}
        ];

        beforeEach(function (done) {
            pinCollection.insert(expectedPins, done);
        });

        afterEach(function () {
            expectedPins.forEach(function (pin) {
                pinCollection.remove({_id: pin._id}, false);
            });
        });

        it('will return all available pins on team.', function (done) {
            var httpGet = supertest.get(path);
            httpGet.cookies = Cookies;
            httpGet.expect('Content-Type', /json/).end(function (error, response) {
                expect(error).to.not.exist;
                response.status.should.equal(200);
                JSON.stringify(response.body).should.equal(JSON.stringify(expectedPins));
                done();
            });
        });

        it('will return error when tribe is not available.', function (done) {
            var httpGet = supertest.get(badTribePath);
            httpGet.cookies = Cookies;
            httpGet.expect('Content-Type', /json/).end(function (error, response) {
                expect(response.body).to.eql([]);
                done();
            });
        });
    });
});