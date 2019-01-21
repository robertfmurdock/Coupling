import Pair from "./Pair";
import Comparators from "./Comparators";
// @ts-ignore
import {pairFromArray, pairingTimeCalculator, historyFromArray, TimeResultValue} from 'engine'

export const NEVER_PAIRED = 'NeverPaired';

const context = pairingTimeCalculator({areEqualPairs: Comparators.areEqualPairsSyntax});

export function calculateTimeSinceLastPartnership(expectedPair: Pair, historyDocuments) {
    let result = context.calculateTimeSinceLastPartnership(
        pairFromArray(expectedPair),
        historyFromArray(historyDocuments)
    );

    if (result instanceof TimeResultValue) {
        return result.time;
    } else {
        return NEVER_PAIRED;
    }
}