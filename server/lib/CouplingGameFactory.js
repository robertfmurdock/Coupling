var CouplingGame = require('./CouplingGame');
var CouplingWheel = require('./CouplingWheel');
var Sequencer = require('./Sequencer');
var PairingHistory = require('./PairingHistory');

function CouplingGameFactory() {
}
CouplingGameFactory.prototype = {
    buildGame: function (historyDocuments) {
        return new CouplingGame(new Sequencer(new PairingHistory(historyDocuments)), new CouplingWheel());
    }};
module.exports = CouplingGameFactory;
