import * as monk from "monk";
import CouplingGameFactory from "../../../../server/lib/CouplingGameFactory";
import GameRunner from "../../../../server/lib/GameRunner";
import CouplingDataService from "../../../../server/lib/CouplingDataService";
import PairAssignmentDocument from "../../../../common/PairAssignmentDocument";
import Comparators from "../../../../server/lib/Comparators";
import Badge from "../../../../common/Badge";

const config = require('../../../../config');

describe('The game', function () {
    const tribeId = 'JLA';

    const bruce = {_id: monk.id(), name: "Batman", tribe: tribeId, badge: Badge.One};
    const hal = {_id: monk.id(), name: "Green Lantern", tribe: tribeId, badge: Badge.One};
    const barry = {_id: monk.id(), name: "Flash", tribe: tribeId, badge: Badge.One};
    const john = {_id: monk.id(), name: "Martian Manhunter", tribe: tribeId, badge: Badge.One};
    const clark = {_id: monk.id(), name: "Superman", tribe: tribeId, badge: Badge.Two};
    const diana = {_id: monk.id(), name: "Wonder Woman", tribe: tribeId, badge: Badge.Two};

    const playerRoster = [
        clark,
        bruce,
        diana,
        hal,
        barry,
        john
    ];

    const mongoUrl = config.testMongoUrl + '/CouplingTest';
    const database = monk(mongoUrl);

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

    it('works with no history', function (done) {
        const couplingGameFactory = new CouplingGameFactory();
        const gameRunner = new GameRunner(couplingGameFactory);


        new CouplingDataService(mongoUrl).requestPlayersAndHistory(tribeId)
            .then(function (both) {
                const result = gameRunner.run(both.players, [], both.history, tribeId);
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
        const couplingGameFactory = new CouplingGameFactory();
        const gameRunner = new GameRunner(couplingGameFactory);

        new CouplingDataService(mongoUrl).requestHistory(tribeId)
            .then(function (history) {
                const result = gameRunner.run([clark, bruce, diana], [], history, tribeId);
                expect(result.pairs.length).toEqual(2);
            })
            .then(done, done.fail);
    });

    it('will always pair someone who has paired with everyone but one person with that one person', function (done) {
        const couplingGameFactory = new CouplingGameFactory();
        const gameRunner = new GameRunner(couplingGameFactory);

        const history = [
            new PairAssignmentDocument(new Date(2014, 1, 10), [
                [bruce, clark]
            ], 'JLA'),
            new PairAssignmentDocument(new Date(2014, 1, 9), [
                [bruce, diana]
            ], 'JLA'),
            new PairAssignmentDocument(new Date(2014, 1, 8), [
                [bruce, hal]
            ], 'JLA'),
            new PairAssignmentDocument(new Date(2014, 1, 7), [
                [bruce, barry]
            ], 'JLA')
        ];

        historyCollection.insert(history, function () {
            new CouplingDataService(mongoUrl).requestPlayersAndHistory(tribeId)
                .then(function (both) {
                    const pairAssignments = gameRunner.run(both.players, [], both.history, tribeId);
                    const foundBruceAndJohn = pairAssignments.pairs.some(function (pair) {
                        return Comparators.areEqualPairs([bruce, john], pair);
                    });
                    expect(foundBruceAndJohn).toBe(true);
                })
                .then(done, done.fail);
        });
    });
});
