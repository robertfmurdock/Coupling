var PairingHistory = function (historyDocuments) {

    var historyWithMostRecentFirst = historyDocuments.reverse();

    var calculateTimeSinceLastPartnership = function (player, availablePartner) {
        var documentsSinceLastPartnership = null;
        historyWithMostRecentFirst.some(function (pairingDocument, indexInHistory) {


            var playerIndex = pairingDocument.pairs.indexOf(player);
            var partnerIndex = pairingDocument.pairs.indexOf(availablePartner);

            if (partnerIndex >= 0 && playerIndex >= 0) {
                documentsSinceLastPartnership = indexInHistory;
                return true;
            }
        });
        return documentsSinceLastPartnership;
    };


    function getAllPartnersWithThisTime(partnersWithTime, timeSinceLastPartnership) {
        if (!partnersWithTime[timeSinceLastPartnership]) {
            partnersWithTime[timeSinceLastPartnership] = [];
        }
        return partnersWithTime[timeSinceLastPartnership];
    }

    return {
        historyDocuments: historyDocuments,

        getPairCandidateReport: function (player, availablePartners) {

            var partnersWithTime = {};

            availablePartners.forEach(function (availablePartner) {
                var timeSinceLastPartnership = calculateTimeSinceLastPartnership(player, availablePartner);

                var allPartnersWithThisTime = getAllPartnersWithThisTime(partnersWithTime, timeSinceLastPartnership);
                allPartnersWithThisTime.push(availablePartner);
            });

            var longestTime = -1;
            Object.keys(partnersWithTime).forEach(function (key) {
                longestTime = Math.max(longestTime, parseInt(key));
            });

            if (longestTime)
                return partnersWithTime[longestTime];
            else
                return availablePartners;
        }
    };

};
module.exports = PairingHistory;