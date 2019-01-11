// @ts-ignore
import {PairingRule, spinContext, historyFromArray, CreateAllPairCandidateReportsCommandDispatcher} from "engine";
import Comparators from "../../common/Comparators";
import {convertToJavascriptPairCandidateReport} from "./PairingHistory";

const context = spinContext({areEqualPairs: Comparators.areEqualPairsSyntax});

export default class ReportProvider extends CreateAllPairCandidateReportsCommandDispatcher {

    constructor(public pairingHistory: any, public actionDispatcher = context) {
        super()
    }

    getPairHistoryReports(players, pairingRule) {
        // @ts-ignore
        return this.createAllPairCandidateReport(
            historyFromArray(this.pairingHistory.historyDocuments),
            players,
            PairingRule.Companion.fromValue(pairingRule)
        ).map(ktReport => convertToJavascriptPairCandidateReport(ktReport));
    }
}
