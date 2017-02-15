import PairingHistory from "./PairingHistory";
import Sequencer from "./Sequencer";
import CouplingGame from "./CouplingGame";
import CouplingWheel from "./CouplingWheel";
import ReportProvider from "./ReportProvider";

export default class CouplingGameFactory {

    buildGame(historyDocuments: any[]) {
        return new CouplingGame(new Sequencer(new ReportProvider(new PairingHistory(historyDocuments))), new CouplingWheel());
    }

}
