import PairHistoryReport from "./PairHistoryReport";

export default class Sequencer {
    constructor(public reportProvider: any) {
    }

    getNextInSequence(players) {
        const allReports = this.reportProvider.getPairHistoryReports(players);
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
