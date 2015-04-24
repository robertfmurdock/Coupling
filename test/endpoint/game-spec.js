"use strict";
var Supertest = require('supertest');
var expect = require('chai').expect;
var config = require('../../config');
var monk = require('monk');

var tribeId = 'test';
var pinId = 'testPin';
var path = '/api/' + tribeId + '/spin';
var host = 'http://localhost:' + config.port;

var database = monk(config.testMongoUrl + '/CouplingTemp');
var pinCollection = database.get('pins');

describe(path, function () {
    var supertest = Supertest(host);
    var Cookies;

    before(function(){
        removeTestPin();
    });

    beforeEach(function (done) {
        supertest.get('/test-login?username="name"&password="pw"').end(function (err, res) {
            Cookies = res.headers['set-cookie'].pop().split(';')[0];
            done(err);
        });
    });

    function removeTestPin() {
        pinCollection.remove({tribe: tribeId});
    }

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
            .expect(200)
            .expect('Content-Type', /json/)
            .end(function (error, response) {
                expect(response.body.tribe).to.equal(tribeId);
                decorateWithPins(onlyEnoughPlayersForOnePair)
                var expectedPairAssignments = [onlyEnoughPlayersForOnePair];
                expect(response.body.pairs).to.eql(expectedPairAssignments);
                done(error);
            });
    });

    describe("when a pin exists", function () {

        var pin = {_id: pinId, tribe: tribeId, name: 'super test pin'};
        beforeEach(function (done) {
            pinCollection.insert(pin, done);
        });

        it('will assign one pin to a player', function (done) {
            var players = [
                {name: "dude1"}
            ];
            var post = supertest.post(path);
            post.cookies = Cookies;
            post.send(players).expect(200)
                .expect('Content-Type', /json/)
                .end(function (error, response) {
                    expect(response.body.tribe).to.equal(tribeId);
                    var expectedPinnedPlayer = {name: "dude1", pins: [pin]};
                    var expectedPairAssignments = [
                        [expectedPinnedPlayer]
                    ];
                    expect(response.body.pairs).to.eql(expectedPairAssignments);
                    done(error);
                });
        });
    });
});