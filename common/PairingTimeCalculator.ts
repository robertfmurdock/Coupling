import Pair from "./Pair";
import Comparators from "./Comparators";
// @ts-ignore
import {PairingTimeCalculationSyntax, CouplingPair} from 'engine'

export const NEVER_PAIRED = 'NeverPaired';

export function calculateTimeSinceLastPartnership(expectedPair: Pair, historyDocuments) {
    const history = PairingTimeCalculationSyntax.Companion.historyFromArray(historyDocuments);
    const pair = CouplingPair.Companion.from(expectedPair);

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