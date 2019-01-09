import Pair from "./Pair";
import Comparators from "./Comparators";
// @ts-ignore
import {CouplingPair, PairingTimeCalculationSyntax, TimeResultValue} from 'engine'

export const NEVER_PAIRED = 'NeverPaired';

const guy = new PairingTimeCalculationSyntax();
guy.couplingComparisionSyntax = {
    areEqualPairs: function (couplingPair1, couplingPair2) {
        return Comparators.areEqualPairs(couplingPair1.asArray(), couplingPair2.asArray());
    }
};

export function calculateTimeSinceLastPartnership(expectedPair: Pair, historyDocuments) {
    let result = guy.calculateTimeSinceLastPartnership(
        CouplingPair.Companion.from(expectedPair),
        PairingTimeCalculationSyntax.Companion.historyFromArray(historyDocuments)
    );

    if (result instanceof TimeResultValue) {
        return result.time;
    } else {
        return NEVER_PAIRED;
    }
}