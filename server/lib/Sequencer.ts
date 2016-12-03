import PairHistoryReport from "./PairHistoryReport";

export default class Sequencer {
    constructor(public pairingHistory: any) {
    }

    getNextInSequence(players) {
        var allReports = [];
        players.forEach(player => {
            var candidates = players.filter(function (otherPlayer) {
                return otherPlayer !== player;
            });

            var pairCandidateReport = this.pairingHistory.getPairCandidateReport(player, candidates);
            allReports.push(pairCandidateReport);
        });

        var reportWithLongestTime = new PairHistoryReport(null, null, -1);
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
