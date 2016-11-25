var PairAssignmentDocument = require('./PairAssignmentDocument');
var PinAssigner = require('./PinAssigner');
var clock = require('./Clock');

var GameRunner = function (gameFactory) {
  this.run = function (players, pins, history) {
    var game = gameFactory.buildGame(history);

    new PinAssigner().assignPins(pins, players);
    var pairs = game.play(players);

    return new PairAssignmentDocument(clock.getDate(), pairs);
  };
};
module.exports = GameRunner;