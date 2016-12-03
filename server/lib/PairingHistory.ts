import Comparators from "./Comparators";
import PairHistoryReport from "./PairHistoryReport";
import Player from "../../common/Player";

export default class PairingHistory {

    constructor(public historyDocuments: any[]) {
    }

    private calculateTimeSinceLastPartnership(expectedPair: Player[]) {
        var documentsSinceLastPartnership = null;
        this.historyDocuments.some(function (pairingDocument, indexInHistory) {
            if (pairingDocument.pairs) {
                var foundPairInThisDocument = pairingDocument.pairs.some(function (pair) {
                    return Comparators.areEqualPairs(pair, expectedPair);
                });
            }
            if (foundPairInThisDocument)
                documentsSinceLastPartnership = indexInHistory;
            return foundPairInThisDocument;
        });
        return documentsSinceLastPartnership;
    }

    private getListOfPartnersWithThisTime(partnersWithTime, timeSinceLastPartnership) {
        var partnersWithParticularTime = partnersWithTime[timeSinceLastPartnership];
        return partnersWithParticularTime ? partnersWithParticularTime : partnersWithTime[timeSinceLastPartnership] = [];
    }

    private createReport(timeToPartnersMap, player: Player) {
        var longestTime = -1;
        Object.keys(timeToPartnersMap).forEach(function (key) {
            longestTime = Math.max(longestTime, parseInt(key));
        });

        var partnerCandidates = longestTime >= 0 ? timeToPartnersMap[longestTime] : timeToPartnersMap[null];
        var timeSinceLastPaired = longestTime >= 0 ? longestTime : undefined;
        return new PairHistoryReport(player, partnerCandidates, timeSinceLastPaired);
    }

    getPairCandidateReport(player: Player, availablePartners: Player[]) {
        var timeToPartnersMap = {};

        availablePartners.forEach(availablePartner => {
            var timeSinceLastPartnership = this.calculateTimeSinceLastPartnership([player, availablePartner]);
            var allPartnersWithThisTime = this.getListOfPartnersWithThisTime(timeToPartnersMap, timeSinceLastPartnership);
            allPartnersWithThisTime.push(availablePartner);
        });

        return this.createReport(timeToPartnersMap, player);
    };

};