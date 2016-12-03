var CouplingGame = require('./CouplingGame');
var CouplingWheel = require('./CouplingWheel');
var Sequencer = require('./Sequencer').default;
var PairingHistory = require('./PairingHistory').default;

function CouplingGameFactory() {
}
CouplingGameFactory.prototype = {
    buildGame: function (historyDocuments) {
        return new CouplingGame(new Sequencer(new PairingHistory(historyDocuments)), new CouplingWheel());
    }};
module.exports = CouplingGameFactory;
