var PairHistoryReport = require('./PairHistoryReport');
var Sequencer = function (pairingHistory) {

    return {
        getNextInSequence: function (players) {
            var allReports = [];
            players.forEach(function (player) {
                var candidates = players.filter(function (otherPlayer) {
                    return otherPlayer !== player;
                });

                var pairCandidateReport = pairingHistory.getPairCandidateReport(player, candidates);
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
    };
};

module.exports = Sequencer;
