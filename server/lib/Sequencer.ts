import PairHistoryReport from "./PairHistoryReport";

export default class Sequencer {
    constructor(public reportProvider: any) {
    }

    private hasNeverPaired(report) {
        return report.timeSinceLastPaired === undefined || report.timeSinceLastPaired === null;
    }

    getNextInSequence(players) {
        const allReports = this.reportProvider.getPairHistoryReports(players);
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
