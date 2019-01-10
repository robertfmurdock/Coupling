import PairCandidateReport from "./PairCandidateReport";
import Player from "../../common/Player";

// @ts-ignore
import {spinContext, historyFromArray, TimeResultValue} from 'engine'
import Comparators from "../../common/Comparators";

const context = spinContext({areEqualPairs: Comparators.areEqualPairsSyntax});

export function convertToJavascriptPairCandidateReport(report) {
    let time = undefined;
    if (report.timeResult instanceof TimeResultValue) {
        time = report.timeResult.time;
    }

    return new PairCandidateReport(
        report.player,
        report.partnersAsArray(),
        time
    );
}

export default class PairingHistory {

    constructor(public historyDocuments: any[]) {
    }

    getPairCandidateReport(player: Player, availablePartners: Player[]) {
        let report = context.createPairCandidateReport(
            historyFromArray(this.historyDocuments),
            player,
            availablePartners
        );
        return convertToJavascriptPairCandidateReport(report);
    };

};