"use strict";
var PairHistoryReport = require('./PairHistoryReport');
var Comparators = require('./Comparators');

var PairingHistory = function (historyDocumentsNewestToOldest) {
    function calculateTimeSinceLastPartnership(expectedPair) {
        var documentsSinceLastPartnership = null;
        historyDocumentsNewestToOldest.some(function (pairingDocument, indexInHistory) {
            var foundPairInThisDocument = pairingDocument.pairs.some(function (pair) {
                return Comparators.areEqualPairs(pair, expectedPair);
            });
            if (foundPairInThisDocument)
                documentsSinceLastPartnership = indexInHistory;
            return foundPairInThisDocument;
        });
        return documentsSinceLastPartnership;
    }

    function getListOfPartnersWithThisTime(partnersWithTime, timeSinceLastPartnership) {
        var partnersWithParticularTime = partnersWithTime[timeSinceLastPartnership];
        return partnersWithParticularTime ? partnersWithParticularTime : partnersWithTime[timeSinceLastPartnership] = [];
    }

    function createReport(timeToPartnersMap, player) {
        var longestTime = -1;
        Object.keys(timeToPartnersMap).forEach(function (key) {
            longestTime = Math.max(longestTime, parseInt(key));
        });

        var partnerCandidates = longestTime ? timeToPartnersMap[longestTime] : timeToPartnersMap[null];
        var timeSinceLastPaired = longestTime ? longestTime : undefined;

        return new PairHistoryReport(player, partnerCandidates, timeSinceLastPaired);
    }

    this.historyDocuments = historyDocumentsNewestToOldest;

    this.getPairCandidateReport = function (player, availablePartners) {
        var timeToPartnersMap = {};

        availablePartners.forEach(function (availablePartner) {
            var timeSinceLastPartnership = calculateTimeSinceLastPartnership([player, availablePartner]);
            var allPartnersWithThisTime = getListOfPartnersWithThisTime(timeToPartnersMap, timeSinceLastPartnership);
            allPartnersWithThisTime.push(availablePartner);
        });

        return createReport(timeToPartnersMap, player);
    };

};
module.exports = PairingHistory;