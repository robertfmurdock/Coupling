import Comparators from "./Comparators";
import PairHistoryReport from "./PairHistoryReport";
import Player from "../../common/Player";

export default class PairingHistory {

    constructor(public historyDocuments: any[]) {
    }

    private calculateTimeSinceLastPartnership(expectedPair: Player[]) {
        let documentsSinceLastPartnership = null;
        this.historyDocuments.some((pairingDocument, indexInHistory) => {

            const pairingExistsInDocument = this.pairingExistsInDocument(pairingDocument, expectedPair);

            if (pairingExistsInDocument) {
                documentsSinceLastPartnership = indexInHistory;
            }
            return pairingExistsInDocument;
        });
        return documentsSinceLastPartnership;
    }

    private pairingExistsInDocument(pairingDocument, expectedPair: Player[]) {
        if (pairingDocument.pairs) {
            return pairingDocument.pairs.some(function (pair) {
                return Comparators.areEqualPairs(pair, expectedPair);
            });
        }
        return false;
    }

    private getListOfPartnersWithThisTime(partnersWithTime, timeSinceLastPartnership) {
        const partnersWithParticularTime = partnersWithTime[timeSinceLastPartnership];
        return partnersWithParticularTime ? partnersWithParticularTime : partnersWithTime[timeSinceLastPartnership] = [];
    }

    private createReport(timeToPartnersMap, player: Player) {
        let longestTime = -1;
        Object.keys(timeToPartnersMap).forEach(function (key) {
            longestTime = Math.max(longestTime, parseInt(key));
        });

        const partnerCandidates = longestTime >= 0 ? timeToPartnersMap[longestTime] : timeToPartnersMap[null];
        const timeSinceLastPaired = longestTime >= 0 ? longestTime : undefined;
        return new PairHistoryReport(player, partnerCandidates, timeSinceLastPaired);
    }

    getPairCandidateReport(player: Player, availablePartners: Player[]) {
        const timeToPartnersMap = {};

        availablePartners.forEach(availablePartner => {
            const timeSinceLastPartnership = this.calculateTimeSinceLastPartnership([player, availablePartner]);
            const allPartnersWithThisTime = this.getListOfPartnersWithThisTime(timeToPartnersMap, timeSinceLastPartnership);
            allPartnersWithThisTime.push(availablePartner);
        });

        return this.createReport(timeToPartnersMap, player);
    };

};