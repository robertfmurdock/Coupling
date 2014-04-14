var GameRunner = require('../../../lib/GameRunner');
var CouplingGameFactory = require('../../../lib/CouplingGameFactory');
var CouplingDatabaseAdapter = require('../../../lib/CouplingDatabaseAdapter');
var PairAssignmentDocument = require('../../../lib/PairAssignmentDocument');
var PairComparator = require('../../../lib/PairComparator');
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

        CouplingDatabaseAdapter(mongoUrl, function (players, history, historyCollection) {
            gameRunner.run(players, history, historyCollection);

            historyCollection.find({}, function (error, documents) {
                should(documents.length).be.eql(1);

                var foundPlayers = [];
                documents[0].pairs.forEach(function (pair) {
                    should(pair.length).eql(2);
                    foundPlayers = foundPlayers.concat(pair);
                });

                should(foundPlayers.length).eql(6);
                testIsComplete();
            });

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
            CouplingDatabaseAdapter(mongoUrl, function (players, history, historyCollection) {
                gameRunner.run(players, history, historyCollection);
                var sortNewestToOldest = {sort: {date: -1}};
                historyCollection.find({}, sortNewestToOldest, function (error, documents) {

                    var foundBruceAndJohn = documents[0].pairs.some(function (pair) {
                        return PairComparator.areEqual([bruce, john], pair);
                    });
                    foundBruceAndJohn.should.be.true;
                    testIsComplete();
                });
            });
        });
    });
});
