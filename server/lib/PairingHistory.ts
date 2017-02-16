import Comparators from "./Comparators";
import PairCandidateReport from "./PairCandidateReport";
import Player from "../../common/Player";
import Pair from "../../common/Pair";

export const NEVER_PAIRED = 'NeverPaired';

export function calculateTimeSinceLastPartnership(expectedPair: Pair, historyDocuments) {
    let documentsSinceLastPartnership: number | string = NEVER_PAIRED;
    historyDocuments.some((pairingDocument, indexInHistory) => {
        const existsInDocument = pairingExistsInDocument(pairingDocument, expectedPair);

        if (existsInDocument) {
            documentsSinceLastPartnership = indexInHistory;
        }
        return existsInDocument;
    });
    return documentsSinceLastPartnership;
}

function pairingExistsInDocument(pairingDocument, expectedPair: Pair) {
    if (pairingDocument.pairs) {
        return pairingDocument.pairs.some(function (pair) {
            return Comparators.areEqualPairs(pair, expectedPair);
        });
    }
    return false;
}

export default class PairingHistory {

    constructor(public historyDocuments: any[]) {
    }

    private getListOfPartnersWithThisTime(timeToPartnersMap, timeSinceLastPartnership) {
        const partnersWithParticularTime = timeToPartnersMap[timeSinceLastPartnership];
        if (partnersWithParticularTime) {
            return partnersWithParticularTime;
        } else {
            const newEmptyList = [];
            timeToPartnersMap[timeSinceLastPartnership] = newEmptyList;
            return newEmptyList;
        }
    }

    private createReport(timeToPartnersMap, player: Player) {
        let longestTime = -1;
        Object.keys(timeToPartnersMap).forEach(function (key) {
            longestTime = Math.max(longestTime, parseInt(key));
        });

        const partnerCandidates = longestTime >= 0 ? timeToPartnersMap[longestTime] : timeToPartnersMap[NEVER_PAIRED];
        const timeSinceLastPaired = longestTime >= 0 ? longestTime : undefined;
        return new PairCandidateReport(player, partnerCandidates, timeSinceLastPaired);
    }

    getPairCandidateReport(player: Player, availablePartners: Player[]) {
        const timeToPartnersMap = {};

        availablePartners.forEach(availablePartner => {
            const timeSinceLastPartnership = calculateTimeSinceLastPartnership([player, availablePartner], this.historyDocuments);
            const allPartnersWithThisTime = this.getListOfPartnersWithThisTime(timeToPartnersMap, timeSinceLastPartnership);
            allPartnersWithThisTime.push(availablePartner);
        });

        return this.createReport(timeToPartnersMap, player);
    };

};