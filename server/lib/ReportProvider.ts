import PairingRuleJs from "../../common/PairingRule";
// @ts-ignore
import {PlayerCandidatesFinder, PairingRule} from "engine";

const fallbackRule = PairingRuleJs.LongestTime;

let playerCandidatesFinder = new PlayerCandidatesFinder();

export default class ReportProvider {
    constructor(public pairingHistory: any) {
    }

    getPairHistoryReports(players, pairingRule) {
        const allReports = [];

        allReports.push(...this.getReportsByRule(players, pairingRule));
        if (allReports.length === 0) {
            allReports.push(...this.getReportsByRule(players, fallbackRule));
        }

        return allReports;
    }

    private getReportsByRule(players, pairingRule) {
        const reportsByRule = [];

        players.forEach(player => {
            // @ts-ignore
            const candidates = playerCandidatesFinder.findCandidates(players, PairingRule.Companion.fromValue(pairingRule), player);

            if (candidates.length > 0 || pairingRule === fallbackRule) {
                const pairCandidateReport = this.pairingHistory.getPairCandidateReport(player, candidates);
                reportsByRule.push(pairCandidateReport);
            }
        });

        return reportsByRule;
    }
}
