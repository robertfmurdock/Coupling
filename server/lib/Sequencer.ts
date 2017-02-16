import PairHistoryReport from "./PairCandidateReport";

export default class Sequencer {
    constructor(public reportProvider: any) {
    }

    private hasNeverPaired(report) {
        return report.timeSinceLastPaired === undefined || report.timeSinceLastPaired === null;
    }

    getNextInSequence(players, pairingRule) {
        const allReports = this.reportProvider.getPairHistoryReports(players, pairingRule);
        let reportWithLongestTime = new PairHistoryReport(null, null, -1);

        allReports.forEach((report) => {

            if (reportWithLongestTime.timeSinceLastPaired === report.timeSinceLastPaired) {
                if (report.partnerCandidates.length < reportWithLongestTime.partnerCandidates.length) {
                    reportWithLongestTime = report;
                }
            } else {
                if (this.hasNeverPaired(report) || reportWithLongestTime.timeSinceLastPaired < report.timeSinceLastPaired) {
                    reportWithLongestTime = report;
                }
            }
        });
        return reportWithLongestTime;
    }
}
