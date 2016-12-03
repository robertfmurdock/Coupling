import PairingHistory from "./PairingHistory";
import Sequencer from "./Sequencer";

var CouplingGame = require('./CouplingGame');
var CouplingWheel = require('./CouplingWheel');

export default class CouplingGameFactory {

    buildGame(historyDocuments: any[]) {
        return new CouplingGame(new Sequencer(new PairingHistory(historyDocuments)), new CouplingWheel());
    }

}
