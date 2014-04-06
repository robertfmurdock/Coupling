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


            var reportWithLongestTime = null;
            allReports.forEach(function (report) {
                if (!reportWithLongestTime || reportWithLongestTime.timeSinceLastPaired < report.timeSinceLastPaired) {
                    reportWithLongestTime = report;
                }
            });
            return reportWithLongestTime;
        }
    };
};

module.exports = Sequencer;
