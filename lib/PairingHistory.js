"use strict";
var PairHistoryReport = require('./PairHistoryReport');

var PairingHistory = function (historyDocumentsNewestToOldest) {
    var calculateTimeSinceLastPartnership = function (player, availablePartner) {
        var documentsSinceLastPartnership = null;
        historyDocumentsNewestToOldest.some(function (pairingDocument, indexInHistory) {

            return pairingDocument.pairs.some(function (pair) {

                function findPerson(target) {
                    return function (person) {
                        if (target === person) {
                            return true;
                        }
                        if (target._id && person._id) {
                            return target._id.toHexString() == person._id.toHexString();
                        }
                    };
                }

                if (pair.some(findPerson(player)) && pair.some(findPerson(availablePartner))) {
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