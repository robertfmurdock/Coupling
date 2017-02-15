"use strict";
import * as monk from "monk";
import * as supertest from "supertest";
import PairAssignmentDocument from "../../common/PairAssignmentDocument";
import Badge from "../../common/Badge";
import Tribe from "../../common/Tribe";
import PairingRule from "../../common/PairingRule";
import Comparators from "../../server/lib/Comparators";
let config = require('../../config');

let server = 'http://localhost:' + config.port;
let superAgent = supertest.agent(server);

let tribeId = 'endpointTest';
let pinId = monk.id();
let path = '/api/' + tribeId + '/spin';

let database = monk(config.testMongoUrl + '/CouplingTemp');
let pinCollection = database.get('pins');
let tribeCollection = database.get('tribes');
let historyCollection = database.get('history');

describe(path, function () {

    beforeEach(function (done) {
        tribeCollection.drop()
            .then(done, done.fail);
    });

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
        pair.forEach(player => player['pins'] = []);
        return pair;
    };

    it('will take the players given and use those for pairing.', function (done) {
        let onlyEnoughPlayersForOnePair = [
            {name: "dude1"},
            {name: "dude2"}
        ];
        const tribe: Tribe = {name: 'test', id: tribeId, pairingRule: PairingRule.LongestTime};
        tribeCollection.insert(tribe)
            .then(function () {
                return superAgent.post(path)
                    .send(onlyEnoughPlayersForOnePair)
                    .expect(200)
                    .expect('Content-Type', /json/);
            })
            .then(function (response) {
                expect(response.body.tribe).toEqual(tribeId);
                decorateWithPins(onlyEnoughPlayersForOnePair);
                let expectedPairAssignments = [onlyEnoughPlayersForOnePair];
                expect(response.body.pairs).toEqual(expectedPairAssignments);
            })
            .then(done, done.fail);
    });

    it('the tribe rule is set to PreferDifferentBadge then it will', function (done) {
        const player1 = {_id: monk.id(), name: "One", tribe: tribeId, badge: Badge.Default};
        const player2 = {_id: monk.id(), name: "Two", tribe: tribeId, badge: Badge.Default};
        const player3 = {_id: monk.id(), name: "Three", tribe: tribeId, badge: Badge.Alternate};
        const player4 = {_id: monk.id(), name: "Four", tribe: tribeId, badge: Badge.Alternate};

        const players = [player1, player2, player3, player4];

        const history = [
            new PairAssignmentDocument(new Date(2014, 1, 10), [
                [player1, player3],
                [player2, player4]
            ], tribeId),
            new PairAssignmentDocument(new Date(2014, 1, 9), [
                [player1, player4],
                [player2, player3]
            ], tribeId),
        ];

        const tribe: Tribe = {name: 'test', id: tribeId, pairingRule: PairingRule.PreferDifferentBadge};
        historyCollection.remove({tribe: tribeId})
            .then(() => tribeCollection.remove({id: tribeId}))
            .then(() => historyCollection.insert(history))
            .then(() => tribeCollection.insert(tribe))
            .then(function () {
                return superAgent.post(path)
                    .send(players)
                    .expect(200)
                    .expect('Content-Type', /json/)
            })
            .then(function (response) {
                expect(response.body.tribe).toEqual(tribeId);
                let expectedPairAssignments = [
                    decorateWithPins([player1, player4]),
                    decorateWithPins([player2, player3]),
                ];

                expect(Comparators.areEqualPairs(response.body.pairs[0], expectedPairAssignments[0])).toBe(true);
                expect(Comparators.areEqualPairs(response.body.pairs[1], expectedPairAssignments[1])).toBe(true);
            })
            .then(done, done.fail);
    });

    it('the tribe rule is set to LongestPair then it will ignore badges', function (done) {
        const player1 = {_id: monk.id(), name: "One", tribe: tribeId, badge: Badge.Default};
        const player2 = {_id: monk.id(), name: "Two", tribe: tribeId, badge: Badge.Default};
        const player3 = {_id: monk.id(), name: "Three", tribe: tribeId, badge: Badge.Alternate};
        const player4 = {_id: monk.id(), name: "Four", tribe: tribeId, badge: Badge.Alternate};

        const players = [player1, player2, player3, player4];

        const history = [
            new PairAssignmentDocument(new Date(2014, 2, 10), [
                [player1, player4],
                [player2, player3]
            ], tribeId),
            new PairAssignmentDocument(new Date(2014, 2, 9), [
                [player1, player3],
                [player2, player4]
            ], tribeId)
        ];

        const tribe: Tribe = {name: 'test', id: tribeId, pairingRule: PairingRule.LongestTime};
        historyCollection.remove({tribe: tribeId})
            .then(() => historyCollection.insert(history))
            .then(() => tribeCollection.insert(tribe))
            .then(function () {
                return superAgent.post(path)
                    .send(players)
                    .expect(200)
                    .expect('Content-Type', /json/)
            })
            .then(function (response) {
                expect(response.body.tribe).toEqual(tribeId);
                let expectedPairAssignments = [
                    decorateWithPins([player1, player2]),
                    decorateWithPins([player3, player4]),
                ];
                expect(Comparators.areEqualPairs(response.body.pairs[0], expectedPairAssignments[0])).toBe(true);
                expect(Comparators.areEqualPairs(response.body.pairs[1], expectedPairAssignments[1])).toBe(true);
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

            tribeCollection.drop()
                .then(function () {
                    return tribeCollection.insert({id: tribeId});
                })
                .then(function () {
                    return superAgent.post(path).send(players)
                        .expect(200)
                        .expect('Content-Type', /json/);
                })
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