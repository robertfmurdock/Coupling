var GameRunner = require('../../../lib/GameRunner');
var CouplingGameFactory = require('../../../lib/CouplingGameFactory');
var CouplingDatabaseAdapter = require('../../../lib/CouplingDatabaseAdapter');
var should = require('should');
var monk = require('monk');

describe('The game', function () {
    var playerRoster = [
        {name: "Superman"},
        {name: "Batman"},
        {name: "Wonder Woman"},
        {name: "Green Lantern"},
        {name: "Flash"},
        {name: "Martian Manhunter"}
    ];

    var mongoUrl = 'localhost/CouplingTest';
    var database = monk(mongoUrl);
    var historyCollection = database.get('history');

    before(function (done) {
        var playersCollection = database.get('players');
        historyCollection.drop();
        playersCollection.drop();
        playersCollection.insert(playerRoster, done);
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
});
