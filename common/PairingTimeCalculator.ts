import Pair from "./Pair";
import Comparators from "./Comparators";
// @ts-ignore
import {PairingTimeCalculationSyntax, CouplingPair, TimeResultValue} from 'engine'

export const NEVER_PAIRED = 'NeverPaired';

const guy = new PairingTimeCalculationSyntax();
guy.couplingComparisionSyntax = {
    areEqualPairs: function (couplingPair1, couplingPair2) {
        return Comparators.areEqualPairs(couplingPair1.asArray(), couplingPair2.asArray());
    }
};

export function calculateTimeSinceLastPartnership(expectedPair: Pair, historyDocuments) {
    const history = PairingTimeCalculationSyntax.Companion.historyFromArray(historyDocuments);
    const pair = CouplingPair.Companion.from(expectedPair);

    let result = guy.calculateTimeSinceLastPartnership(pair, history);

    if (result instanceof TimeResultValue) {
        return result.time;
    } else {
        return NEVER_PAIRED;
    }
}

function pairingExistsInDocument(pairingDocument, expectedPair: Pair) {
    if (pairingDocument.pairs) {
        return pairingDocument.pairs.some(function (pair) {
            return Comparators.areEqualPairs(pair, expectedPair);
        });
    }
    return false;
}