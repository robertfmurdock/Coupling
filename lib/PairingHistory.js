var PairingHistory = function (historyDocumentsNewestToOldest) {
    var calculateTimeSinceLastPartnership = function (player, availablePartner) {
        var documentsSinceLastPartnership = null;
        historyDocumentsNewestToOldest.some(function (pairingDocument, indexInHistory) {


            var playerIndex = pairingDocument.pairs.indexOf(player);
            var partnerIndex = pairingDocument.pairs.indexOf(availablePartner);

            if (partnerIndex >= 0 && playerIndex >= 0) {
                documentsSinceLastPartnership = indexInHistory;
                return true;
            }
        });
        return documentsSinceLastPartnership;
    };


    function getListOfPartnersWithThisTime(partnersWithTime, timeSinceLastPartnership) {
        if (!partnersWithTime[timeSinceLastPartnership]) {
            partnersWithTime[timeSinceLastPartnership] = [];
        }
        return partnersWithTime[timeSinceLastPartnership];
    }

    return {
        historyDocuments: historyDocumentsNewestToOldest,

        getPairCandidateReport: function (player, availablePartners) {

            var timeToPartnersMap = {};

            availablePartners.forEach(function (availablePartner) {
                var timeSinceLastPartnership = calculateTimeSinceLastPartnership(player, availablePartner);

                var allPartnersWithThisTime = getListOfPartnersWithThisTime(timeToPartnersMap, timeSinceLastPartnership);
                allPartnersWithThisTime.push(availablePartner);
            });

            var longestTime = -1;
            Object.keys(timeToPartnersMap).forEach(function (key) {
                longestTime = Math.max(longestTime, parseInt(key));
            });

            var partnerCandidates = longestTime ? timeToPartnersMap[longestTime] : availablePartners;
            return  {
                partnerCandidates: partnerCandidates,
                timeSinceLastPaired: longestTime ? longestTime : undefined
            };
        }
    };

};
module.exports = PairingHistory;