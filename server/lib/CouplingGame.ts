import Comparators from "../../common/Comparators";
// @ts-ignore
import {SpinCommandDispatcher, historyFromArray, PairingRule, spinContext} from "engine";

const context = spinContext({areEqualPairs: Comparators.areEqualPairsSyntax});
export default class CouplingGame extends SpinCommandDispatcher {

    constructor(public sequencer, public wheel, public actionDispatcher = context) {
        super()
    }

    public play(playersRoster, pairingRule) {
        // @ts-ignore
        return this.runSpinCommand(
            historyFromArray(this.sequencer.reportProvider.pairingHistory.historyDocuments),
            playersRoster,
            PairingRule.Companion.fromValue(pairingRule)
        )
    };
};