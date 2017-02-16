import PairAssignmentDocument from "../../common/PairAssignmentDocument";
import * as _ from "underscore";
import Comparators from "../../common/Comparators";
import Pair from "../../common/Pair";

const heatIncrements = [0, 1, 2.5, 4.5, 7, 10];
const rotationHeatWindow = 5;

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
        return _.chain(recentHistory)
            .filter(this.filterForIntervalsThatContain(pair))
            .size()
            .value();
    }

    private filterForIntervalsThatContain(pair: Pair) {
        return (document: PairAssignmentDocument) => {
            const result = _.find(document.pairs, docPair => Comparators.areEqualPairs(docPair, pair));
            return result !== undefined;
        };
    }

    private getHistoryInHeatWindow(history: PairAssignmentDocument[], rotationPeriod: number) {
        return history.slice(0, rotationPeriod * rotationHeatWindow);
    }

}