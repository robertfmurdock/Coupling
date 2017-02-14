import PairHistoryReport from "./PairHistoryReport";

export default class Sequencer {
    constructor(public pairingHistory: any) {
    }

    getNextInSequence(players) {
        const allReports = [];
        players.forEach(player => {
            const candidates = players.filter(function (otherPlayer) {
                return otherPlayer !== player;
            });

            const pairCandidateReport = this.pairingHistory.getPairCandidateReport(player, candidates);
            allReports.push(pairCandidateReport);
        });

        let reportWithLongestTime = new PairHistoryReport(null, null, -1);
        allReports.forEach(function (report) {
            if (reportWithLongestTime.timeSinceLastPaired === report.timeSinceLastPaired) {
                if (report.partnerCandidates.length < reportWithLongestTime.partnerCandidates.length) {
                    reportWithLongestTime = report;
                }
            } else {
                if (!report.timeSinceLastPaired || reportWithLongestTime.timeSinceLastPaired < report.timeSinceLastPaired) {
                    reportWithLongestTime = report;
                }
            }
        });
        return reportWithLongestTime;
    }
}
