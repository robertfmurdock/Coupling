var GameRunner = function (gameFactory) {

    this.run = function (players, history, historyCollection) {
        var game = gameFactory.buildGame();

        var pairs = game.play(players);
        var pairAssignmentsDocument = {
            pairs: pairs,
            date: new Date()
        };
        historyCollection.insert(pairAssignmentsDocument);
    };
};
GameRunner.prototype = {};
module.exports = GameRunner;