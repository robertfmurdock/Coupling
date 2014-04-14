"use strict";
var PairHistoryReport = require('./PairHistoryReport');
var PairComparator = require('./PairComparator');

var PairingHistory = function (historyDocumentsNewestToOldest) {
    var calculateTimeSinceLastPartnership = function (player, availablePartner) {
        var documentsSinceLastPartnership = null;

        historyDocumentsNewestToOldest.some(function (pairingDocument, indexInHistory) {

            return pairingDocument.pairs.some(function (pair) {

                if (PairComparator.areEqual(pair, [player, availablePartner])) {
                    documentsSinceLastPartnership = indexInHistory;
                    return true;
                }
            });
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

            var partnerCandidates = longestTime ? timeToPartnersMap[longestTime] : timeToPartnersMap[null];
            var timeSinceLastPaired = longestTime ? longestTime : undefined;


            return new PairHistoryReport(player, partnerCandidates, timeSinceLastPaired);
        }
    };

};
module.exports = PairingHistory;