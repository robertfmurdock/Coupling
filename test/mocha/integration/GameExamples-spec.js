var GameRunner = require('../../../server/lib/GameRunner');
var CouplingGameFactory = require('../../../server/lib/CouplingGameFactory');
var CouplingDataService = require('../../../server/lib/CouplingDataService');
var PairAssignmentDocument = require('../../../server/lib/PairAssignmentDocument');
var Comparators = require('../../../server/lib/Comparators');
var expect = require('chai').expect;
var monk = require('monk');

describe('The game', function () {
    var clark = {name: "Superman"};
    var bruce = {name: "Batman"};
    var diana = {name: "Wonder Woman"};
    var hal = {name: "Green Lantern"};
    var barry = {name: "Flash"};
    var john = {name: "Martian Manhunter"};
    var playerRoster = [
        clark,
        bruce,
        diana,
        hal,
        barry,
        john
    ];

    var config = require('../../../config');
    var mongoUrl = config.testMongoUrl + '/CouplingTest';
    var database = monk(mongoUrl);
    var historyCollection = database.get('history');

    before(function (done) {
        var playersCollection = database.get('players');
        playersCollection.drop();
        playersCollection.insert(playerRoster, done);
    });

    beforeEach(function () {
        historyCollection.drop();
    });

    it('works with no history', function (testIsComplete) {
        var couplingGameFactory = new CouplingGameFactory();
        var gameRunner = new GameRunner(couplingGameFactory);

        new CouplingDataService(mongoUrl).requestPlayersAndHistory(null).then(function (both) {
            var result = gameRunner.run(both.players, [], both.history);
            var foundPlayers = [];
            result.pairs.forEach(function (pair) {
                expect(pair.length).eql(2);
                foundPlayers = foundPlayers.concat(pair);
            });

            expect(foundPlayers.length).eql(6);
            testIsComplete();
        }).catch(testIsComplete);
    });

    it('works with an odd number of players history', function (testIsComplete) {
        var couplingGameFactory = new CouplingGameFactory();
        var gameRunner = new GameRunner(couplingGameFactory);

        new CouplingDataService(mongoUrl).requestHistory(null).then(function (history) {
            var result = gameRunner.run([clark, bruce, diana], [], history);
            expect(result.pairs.length).eql(2);
            testIsComplete();
        }).catch(testIsComplete);
    });

    it('will always pair someone who has paired with everyone but one person with that one person', function (testIsComplete) {
        var couplingGameFactory = new CouplingGameFactory();
        var gameRunner = new GameRunner(couplingGameFactory);

        var history = [
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

        historyCollection.insert(history, function () {
            new CouplingDataService(mongoUrl).requestPlayersAndHistory(null).then(function (both) {
                var pairAssignments = gameRunner.run(both.players, [], both.history);
                var foundBruceAndJohn = pairAssignments.pairs.some(function (pair) {
                    return Comparators.areEqualPairs([bruce, john], pair);
                });
                expect(foundBruceAndJohn).to.be.true;
                testIsComplete();
            }).catch(testIsComplete);
        });
    });
});
