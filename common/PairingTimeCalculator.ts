import Pair from "./Pair";
import Comparators from "./Comparators";
// @ts-ignore
import {CouplingPair, PairingTimeCalculationSyntax, historyFromArray, TimeResultValue} from 'engine'

export const NEVER_PAIRED = 'NeverPaired';

const context = new PairingTimeCalculationSyntax();
context.couplingComparisionSyntax = {areEqualPairs: Comparators.areEqualPairsSyntax};

export function calculateTimeSinceLastPartnership(expectedPair: Pair, historyDocuments) {
    let result = context.calculateTimeSinceLastPartnership(
        CouplingPair.Companion.from(expectedPair),
        historyFromArray(historyDocuments)
    );

    if (result instanceof TimeResultValue) {
        return result.time;
    } else {
        return NEVER_PAIRED;
    }
}