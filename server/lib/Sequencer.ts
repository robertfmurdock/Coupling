// @ts-ignore
import {GetNextPairActionDispatcher, historyFromArray, PairingRule, spinContext} from "engine";
import {convertToJavascriptPairCandidateReport} from "./PairingHistory";
import Comparators from "../../common/Comparators";

const context = spinContext({areEqualPairs: Comparators.areEqualPairsSyntax});

export default class Sequencer extends GetNextPairActionDispatcher {
    constructor(public reportProvider: any, public actionDispatcher = context) {
        super()
    }

    getNextInSequence(players, pairingRule) {
        // @ts-ignore
        const ktReport = this.getNextPair(
            historyFromArray(this.reportProvider.pairingHistory.historyDocuments),
            players,
            PairingRule.Companion.fromValue(pairingRule)
        );
        return convertToJavascriptPairCandidateReport(ktReport);
    }

}
