import PairingRule from "../../common/PairingRule";

const fallbackRule = PairingRule.LongestTime;

export default class ReportProvider {
    constructor(public pairingHistory: any) {
    }

    getPairHistoryReports(players, pairingRule) {
        const allReports = [];

        allReports.push(...this.getReportsByRule(players, pairingRule));
        if(allReports.length === 0) {
            allReports.push(...this.getReportsByRule(players, fallbackRule));
        }

        return allReports;
    }

    private getReportsByRule(players, pairingRule) {
        const reportsByRule = [];

        players.forEach(player => {
            const candidates = players.filter(function (otherPlayer) {
                if (pairingRule === PairingRule.PreferDifferentBadge) {
                    return otherPlayer !== player && otherPlayer.badge !== player.badge;
                } else {
                    return otherPlayer !== player;
                }
            });

            if (candidates.length > 0 || pairingRule === fallbackRule) {
                const pairCandidateReport = this.pairingHistory.getPairCandidateReport(player, candidates);
                reportsByRule.push(pairCandidateReport);
            }
        });

        return reportsByRule;
    }
}
