var GameRunner = require('../../../lib/GameRunner');
var CouplingGameFactory = require('../../../lib/CouplingGameFactory');
var CouplingDataService = require('../../../lib/CouplingDataService');
var PairAssignmentDocument = require('../../../lib/PairAssignmentDocument');
var Comparators = require('../../../lib/Comparators');
var should = require('should');
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

    var mongoUrl = 'localhost/CouplingTest';
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

        new CouplingDataService(mongoUrl).requestPlayersAndHistory(null, function (players, history) {
            var result = gameRunner.run(players, history);
            var foundPlayers = [];
            result.pairs.forEach(function (pair) {
                should(pair.length).eql(2);
                foundPlayers = foundPlayers.concat(pair);
            });

            should(foundPlayers.length).eql(6);
            testIsComplete();
        });
    });

    it('works with an odd number of players history', function (testIsComplete) {
        var couplingGameFactory = new CouplingGameFactory();
        var gameRunner = new GameRunner(couplingGameFactory);

        new CouplingDataService(mongoUrl).requestHistory(null, function (history) {
            var result = gameRunner.run([clark, bruce, diana], history);
            should(result.pairs.length).eql(2);
            testIsComplete();
        });
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
            new CouplingDataService(mongoUrl).requestPlayersAndHistory(null, function (players, history) {
                var pairAssignments = gameRunner.run(players, history);
                var foundBruceAndJohn = pairAssignments.pairs.some(function (pair) {
                    return Comparators.areEqualPairs([bruce, john], pair);
                });
                foundBruceAndJohn.should.be.true;
                testIsComplete();
            });
        });
    });
});
