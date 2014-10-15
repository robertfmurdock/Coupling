"use strict";
var Supertest = require('supertest');
var should = require('should');
var config = require('../../config');
var monk = require('monk');

var tribeId = 'test';
var pinId = 'testPin';
var path = '/api/' + tribeId + '/spin';
var host = 'http://localhost:' + config.port;

var database = monk(config.mongoUrl);
var pinCollection = database.get('pins');

describe(path, function () {
    var supertest = Supertest(host);
    var Cookies;

    beforeEach(function (done) {
        supertest.get('/test-login?username="name"&password="pw"').end(function (err, res) {
            should.not.exist(err);
            Cookies = res.headers['set-cookie'].pop().split(';')[0];
            done();
        });
    });

    function removeTestPin() {
        pinCollection.remove({_id: pinId}, false);
    }

    removeTestPin();

    afterEach(function () {
        removeTestPin();
    });

    var decorateWithPins = function (pair) {
        pair.forEach(function (player) {
            player.pins = []
        })
    };

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
                decorateWithPins(onlyEnoughPlayersForOnePair)
                var expectedPairAssignments = [onlyEnoughPlayersForOnePair];
                JSON.stringify(response.body.pairs).should.equal(JSON.stringify(expectedPairAssignments));
                done();
            });
    });

    describe("when a pin exists", function () {

        var pin = {_id: pinId, tribe: tribeId, name: 'super test pin'};
        beforeEach(function (done) {
            pinCollection.insert(pin, done);
        })

        it('will assign one pin to a player', function (done) {
            var players = [
                {name: "dude1"}
            ];
            var post = supertest.post(path);
            post.cookies = Cookies;
            post.send(players)
                .expect('Content-Type', /json/)
                .end(function (error, response) {
                    should.not.exist(error);
                    response.status.should.equal(200);
                    response.body.tribe.should.equal(tribeId);
                    var expectedPinnedPlayer = {name: "dude1", pins: [ pin ]};
                    var expectedPairAssignments = [
                        [ expectedPinnedPlayer ]
                    ];
                    JSON.stringify(response.body.pairs).should.equal(JSON.stringify(expectedPairAssignments));
                    done();
                });
        });
    });
});