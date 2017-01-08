"use strict";
import * as monk from "monk";
import * as supertest from "supertest-as-promised";
let config = require('../../config');

let server = 'http://localhost:' + config.port;
let superAgent = supertest.agent(server);

let tribeId = 'endpointTest';
let pinId = monk.id();
let path = '/api/' + tribeId + '/spin';

let database = monk(config.testMongoUrl + '/CouplingTemp');
let pinCollection = database.get('pins');

describe(path, function () {

    beforeAll(function () {
        removeTestPin();
    });


    beforeEach(function (done) {
        superAgent.get('/test-login?username="name"&password="pw"')
            .expect(302)
            .end(done);
    });

    function removeTestPin() {
        pinCollection.remove({tribe: tribeId});
    }

    afterEach(function () {
        removeTestPin();
    });

    let decorateWithPins = function (pair) {
        pair.forEach(player => player['pins'] = [])
    };

    it('will take the players given and use those for pairing.', function (done) {
        let onlyEnoughPlayersForOnePair = [
            {name: "dude1"},
            {name: "dude2"}
        ];
        superAgent.post(path)
            .send(onlyEnoughPlayersForOnePair)
            .expect(200)
            .expect('Content-Type', /json/)
            .then(function (response) {
                expect(response.body.tribe).toEqual(tribeId);
                decorateWithPins(onlyEnoughPlayersForOnePair);
                let expectedPairAssignments = [onlyEnoughPlayersForOnePair];
                expect(response.body.pairs).toEqual(expectedPairAssignments);
            })
            .then(done, done.fail);
    });

    describe("when a pin exists", function () {

        let pin = {_id: pinId, tribe: tribeId, name: 'super test pin'};

        beforeEach(function (done) {
            pinCollection.insert(pin)
                .then(done, done.fail);
        });

        it('will assign one pin to a player', function (done) {
            let players = [
                {name: "dude1"}
            ];
            superAgent.post(path).send(players)
                .expect(200)
                .expect('Content-Type', /json/)
                .then(function (response) {
                    expect(response.body.tribe).toEqual(tribeId);
                    let expectedPinnedPlayer = {name: "dude1", pins: [pin]};
                    let expectedPairAssignments = [
                        [expectedPinnedPlayer]
                    ];
                    expect(JSON.stringify(response.body.pairs))
                        .toEqual(JSON.stringify(expectedPairAssignments));
                })
                .then(done, done.fail);
        });
    });
});