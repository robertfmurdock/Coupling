import PairAssignmentDocument from "../../common/PairAssignmentDocument";
import Pair from "../../common/Pair";
import * as _ from "underscore";
import Comparators from "./Comparators";

const heatIncrements = [1, 2.5, 4.5, 7, 10];
const rotationHeatWindow = 5;

export default class PairHeatCalculator {

    calculate(pair: Pair, history: PairAssignmentDocument[], rotationPeriod: number) {

        const pairingHistory = _.filter(this.getRecentHistory(history, rotationPeriod), document => {
            const matchingPair = _.find(document.pairs, docPair => Comparators.areEqualPairs(docPair, pair));
            return matchingPair !== undefined;
        });

        if (pairingHistory.length === 0) {
            return 0;
        }

        return heatIncrements[pairingHistory.length - 1];
    }

    private getRecentHistory(history: PairAssignmentDocument[], rotationPeriod: number) {
        return history.slice(0, rotationPeriod * rotationHeatWindow);
    }

}