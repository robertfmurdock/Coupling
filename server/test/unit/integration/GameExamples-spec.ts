import * as monk from "monk";
import GameRunner from "../../../lib/GameRunner";
import CouplingDataService from "../../../lib/CouplingDataService";
import PairAssignmentDocument from "../../../../common/PairAssignmentDocument";
import Comparators from "../../../../common/Comparators";
import Badge from "../../../../common/Badge";
import PairingRule from "../../../../common/PairingRule";
import Pair from "../../../../common/Pair";

const config = require('../../../config/config');

describe('The game', function () {

    const mongoUrl = config.testMongoUrl + '/CouplingTest';
    const database = monk.default(mongoUrl);
    const playersCollection = database.get('players');
    const historyCollection = database.get('history');

    describe('with uniform badges and longest pair rule', function () {
        const tribeId = 'JLA';

        const bruce = {_id: monk.id(), name: "Batman", tribe: tribeId, badge: Badge.Default};
        const hal = {_id: monk.id(), name: "Green Lantern", tribe: tribeId, badge: Badge.Default};
        const barry = {_id: monk.id(), name: "Flash", tribe: tribeId, badge: Badge.Default};
        const john = {_id: monk.id(), name: "Martian Manhunter", tribe: tribeId, badge: Badge.Default};
        const clark = {_id: monk.id(), name: "Superman", tribe: tribeId, badge: Badge.Default};
        const diana = {_id: monk.id(), name: "Wonder Woman", tribe: tribeId, badge: Badge.Default};

        const playerRoster = [
            clark,
            bruce,
            diana,
            hal,
            barry,
            john
        ];

        beforeEach(function (done) {
            playersCollection.drop()
                .then(function () {
                    return playersCollection.insert(playerRoster);
                })
                .then(done, done.fail)
        });

        beforeEach(function () {
            historyCollection.drop();
        });

        it('works with no history', function (done) {
            const gameRunner = new GameRunner();

            const tribe = {id: tribeId, pairingRule: PairingRule.LongestTime};

            new CouplingDataService(mongoUrl).requestPlayersAndHistory(tribeId)
                .then(function (both) {
                    const result = gameRunner.run(both.players, [], both.history, tribe);
                    let foundPlayers = [];
                    result.pairs.forEach(function (pair) {
                        expect(pair.length).toEqual(2);
                        foundPlayers = foundPlayers.concat(pair);
                    });

                    expect(foundPlayers.length).toEqual(6);
                })
                .then(done, done.fail);
        });

        it('works with an odd number of players history', function (done) {
            const tribe = {id: tribeId, pairingRule: PairingRule.LongestTime};
            const gameRunner = new GameRunner();

            new CouplingDataService(mongoUrl).requestHistory(tribeId)
                .then(function (history) {
                    const result = gameRunner.run([clark, bruce, diana], [], history, tribe);
                    expect(result.pairs.length).toEqual(2);
                })
                .then(done, done.fail);
        });

        it('will always pair someone who has paired with everyone but one person with that one person', function (done) {
            const gameRunner = new GameRunner();

            const history: any[] = [
                new PairAssignmentDocument(new Date(2014, 1, 10), [
                    [bruce, clark]
                ]),
                new PairAssignmentDocument(new Date(2014, 1, 9), [
                    [bruce, diana]
                ]),
                new PairAssignmentDocument(new Date(2014, 1, 8), [
                    [bruce, hal]
                ]),
                new PairAssignmentDocument(new Date(2014, 1, 7), [
                    [bruce, barry]
                ])
            ];

            history.forEach(doc => doc.tribe = tribeId);

            historyCollection.insert(history, function () {
                new CouplingDataService(mongoUrl).requestPlayersAndHistory(tribeId)
                    .then(function (both) {
                        const pairAssignments = gameRunner.run(both.players, [], both.history, {id: tribeId});
                        const foundBruceAndJohn = pairAssignments.pairs.some(function (pair: Pair) {
                            return Comparators.areEqualPairs([bruce, john], pair);
                        });
                        expect(foundBruceAndJohn).toBe(true);
                    })
                    .then(done, done.fail);
            });
        });
    });
    describe('with different badges and longest pair rule', function () {
        const tribeId = 'JLA';

        const bruce = {_id: monk.id(), name: "Batman", tribe: tribeId, badge: Badge.Default};
        const hal = {_id: monk.id(), name: "Green Lantern", tribe: tribeId, badge: Badge.Alternate};
        const barry = {_id: monk.id(), name: "Flash", tribe: tribeId, badge: Badge.Default};
        const john = {_id: monk.id(), name: "Martian Manhunter", tribe: tribeId, badge: Badge.Alternate};
        const clark = {_id: monk.id(), name: "Superman", tribe: tribeId, badge: Badge.Default};
        const diana = {_id: monk.id(), name: "Wonder Woman", tribe: tribeId, badge: Badge.Alternate};

        const playerRoster = [
            clark,
            bruce,
            diana,
            hal,
            barry,
            john
        ];

        const mongoUrl = config.testMongoUrl + '/CouplingTest';
        const database = monk.default(mongoUrl);

        const historyCollection = database.get('history');

        beforeEach(function (done) {
            const playersCollection = database.get('players');
            playersCollection.drop()
                .then(function () {
                    return playersCollection.insert(playerRoster);
                })
                .then(done, done.fail)
        });

        beforeEach(function () {
            historyCollection.drop();
        });

        it('will always pair someone who has paired with everyone but one person with that one person', function (done) {
            const gameRunner = new GameRunner();

            const history: any[] = [
                new PairAssignmentDocument(new Date(2014, 1, 10), [
                    [bruce, clark]
                ]),
                new PairAssignmentDocument(new Date(2014, 1, 9), [
                    [bruce, diana]
                ]),
                new PairAssignmentDocument(new Date(2014, 1, 8), [
                    [bruce, hal]
                ]),
                new PairAssignmentDocument(new Date(2014, 1, 7), [
                    [bruce, barry]
                ])
            ];

            history.forEach(doc => doc.tribe = tribeId);

            const tribe = {id: tribeId, pairingRule: PairingRule.LongestTime};

            historyCollection.insert(history)
                .then(function () {
                    return new CouplingDataService(mongoUrl).requestPlayersAndHistory(tribeId)
                })
                .then(function (both) {
                    const pairAssignments = gameRunner.run(both.players, [], both.history, tribe);
                    const foundBruceAndJohn = pairAssignments.pairs.some(function (pair: Pair) {
                        return Comparators.areEqualPairs([bruce, john], pair);
                    });
                    expect(foundBruceAndJohn).toBe(true);
                })
                .then(done, done.fail);
        });
    });

    it('will not get stuck when pairing people with different badges', function (done) {
        const tribeId = 'Avengers';

        const kamala = {_id: monk.id(), name: "Ms. Marvel", tribe: tribeId, badge: Badge.Default};
        const logan = {_id: monk.id(), name: "Wolverine", tribe: tribeId, badge: Badge.Alternate};
        const steve = {_id: monk.id(), name: "Captain America", tribe: tribeId, badge: Badge.Alternate};
        const thor = {_id: monk.id(), name: "Thor", tribe: tribeId, badge: Badge.Alternate};

        const playerRoster = [
            kamala,
            logan,
            steve,
            thor
        ];

        const history = [
            new PairAssignmentDocument(new Date(2014, 1, 10), [
                [kamala, thor]
            ]),
            new PairAssignmentDocument(new Date(2014, 1, 9), [
                [kamala, steve]
            ]),
            new PairAssignmentDocument(new Date(2014, 1, 8), [
                [kamala, logan]
            ])
        ];

        const gameRunner = new GameRunner();

        const tribe = {id: tribeId, pairingRule: PairingRule.PreferDifferentBadge};

        saveAndLoadData(playerRoster, history, tribeId)
            .then(function (both) {
                const pairAssignments = gameRunner.run(both.players, [], both.history, tribe);
                const foundKamalaLoganPair = pairAssignments.pairs.some(function (pair: Pair) {
                    return Comparators.areEqualPairs([kamala, logan], pair);
                });
                expect(foundKamalaLoganPair).toBe(true);
            })
            .then(done, done.fail);
    });

    function saveAndLoadData(playerRoster, history, tribeId) {
        return playersCollection.drop()
            .then(function () {
                return historyCollection.drop();
            })
            .then(function () {
                return playersCollection.insert(playerRoster);
            })
            .then(function () {
                history.forEach(doc => doc.tribe = tribeId);
                return historyCollection.insert(history);
            })
            .then(function () {
                return new CouplingDataService(mongoUrl).requestPlayersAndHistory(tribeId);
            });
    }
});
