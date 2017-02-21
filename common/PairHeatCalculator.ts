import PairAssignmentDocument from "./PairAssignmentDocument";
import * as filter from "ramda/src/filter";
import * as pipe from "ramda/src/pipe";
import * as unnest from "ramda/src/unnest";
import * as curry from "ramda/src/curry";
import * as prop from "ramda/src/prop";
import * as map from "ramda/src/map";
import * as length from "ramda/src/length";
import Comparators from "./Comparators";
import Pair from "./Pair";
import Player from "./Player";
import PairAssignmentSet from "./PairAssignmentSet";

const heatIncrements = [0, 1, 2.5, 4.5, 7, 10];
const rotationHeatWindow = 5;

const equalPairs = curry(Comparators.areEqualPairs);

const getRecentPairs: (recentHistory: PairAssignmentSet[]) => Pair[] = pipe(
    map(prop('pairs')),
    unnest
);

function countMatches(pair: Player[], recentPairs: Pair[]) {
    return pipe(
        filter(equalPairs(pair)),
        length
    )(recentPairs)
}


export default class PairHeatCalculator {

    calculate(pair: Pair, history: PairAssignmentDocument[], rotationPeriod: number) {
        const recentHistory = this.getHistoryInHeatWindow(history, rotationPeriod);
        const timesPairedInHeatWindow = this.calculateTimesPaired(pair, recentHistory);
        return this.getHeatValue(timesPairedInHeatWindow);
    }

    private getHeatValue(timesPairedInHeatWindow: number) {
        const index = Math.min(timesPairedInHeatWindow, heatIncrements.length - 1);
        return heatIncrements[index];
    }

    private calculateTimesPaired(pair: Pair, recentHistory: PairAssignmentDocument[]) {
        const recentPairs = getRecentPairs(recentHistory);
        return countMatches(pair, recentPairs);
    }

    private getHistoryInHeatWindow(history: PairAssignmentDocument[], rotationPeriod: number) {
        return history.slice(0, rotationPeriod * rotationHeatWindow);
    }

}