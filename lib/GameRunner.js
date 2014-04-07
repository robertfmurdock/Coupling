var PairAssignmentDocument = require('./PairAssignmentDocument');

var GameRunner = function (gameFactory) {

    this.run = function (players, history, historyCollection) {
        var game = gameFactory.buildGame();

        var pairs = game.play(players);
        var pairAssignmentsDocument = new PairAssignmentDocument(new Date(), pairs);
        historyCollection.insert(pairAssignmentsDocument);
    };
};
GameRunner.prototype = {};
module.exports = GameRunner;