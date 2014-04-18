var PairAssignmentDocument = require('./PairAssignmentDocument');

var GameRunner = function (gameFactory) {
    this.run = function (players, history) {
        var game = gameFactory.buildGame(history);

        var pairs = game.play(players);
        return new PairAssignmentDocument(new Date(), pairs);
    };
};
module.exports = GameRunner;