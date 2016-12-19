import * as monk from "monk";
import CouplingGameFactory from "../../../../server/lib/CouplingGameFactory";
import GameRunner from "../../../../server/lib/GameRunner";
import CouplingDataService from "../../../../server/lib/CouplingDataService";
import PairAssignmentDocument from "../../../../common/PairAssignmentDocument";
import Comparators from "../../../../server/lib/Comparators";

var config = require('../../../../config');

describe('The game', function () {
    var tribeId = 'JLA';
    var clark = {_id: monk.id(), name: "Superman", tribe: tribeId};
    var bruce = {_id: monk.id(), name: "Batman", tribe: tribeId};
    var diana = {_id: monk.id(), name: "Wonder Woman", tribe: tribeId};
    var hal = {_id: monk.id(), name: "Green Lantern", tribe: tribeId};
    var barry = {_id: monk.id(), name: "Flash", tribe: tribeId};
    var john = {_id: monk.id(), name: "Martian Manhunter", tribe: tribeId};

    var playerRoster = [
        clark,
        bruce,
        diana,
        hal,
        barry,
        john
    ];

    var mongoUrl = config.testMongoUrl + '/CouplingTest';
    var database = monk(mongoUrl);

    var historyCollection = database.get('history');

    beforeEach(function (done) {
        var playersCollection = database.get('players');
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
        var couplingGameFactory = new CouplingGameFactory();
        var gameRunner = new GameRunner(couplingGameFactory);


        new CouplingDataService(mongoUrl).requestPlayersAndHistory(tribeId)
            .then(function (both) {
                var result = gameRunner.run(both.players, [], both.history, tribeId);
                var foundPlayers = [];
                result.pairs.forEach(function (pair) {
                    expect(pair.length).toEqual(2);
                    foundPlayers = foundPlayers.concat(pair);
                });

                expect(foundPlayers.length).toEqual(6);
            })
            .then(done, done.fail);
    });

    it('works with an odd number of players history', function (done) {
        var couplingGameFactory = new CouplingGameFactory();
        var gameRunner = new GameRunner(couplingGameFactory);

        new CouplingDataService(mongoUrl).requestHistory(tribeId)
            .then(function (history) {
                var result = gameRunner.run([clark, bruce, diana], [], history, tribeId);
                expect(result.pairs.length).toEqual(2);
            })
            .then(done, done.fail);
    });

    it('will always pair someone who has paired with everyone but one person with that one person', function (done) {
        var couplingGameFactory = new CouplingGameFactory();
        var gameRunner = new GameRunner(couplingGameFactory);

        var history = [
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
                    var pairAssignments = gameRunner.run(both.players, [], both.history, tribeId);
                    var foundBruceAndJohn = pairAssignments.pairs.some(function (pair) {
                        return Comparators.areEqualPairs([bruce, john], pair);
                    });
                    expect(foundBruceAndJohn).toBe(true);
                })
                .then(done, done.fail);
        });
    });
});
