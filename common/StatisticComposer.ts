import * as _ from "underscore";
import PairAssignmentDocument from "./PairAssignmentDocument";
import {calculateTimeSinceLastPartnership, NEVER_PAIRED} from "./PairingTimeCalculator";
import Pair from "./Pair";
import Tribe from "./Tribe";
import Player from "./Player";
import * as parse from "date-fns/parse"
import * as distanceInWords from "date-fns/distance_in_words"

interface PairReport {
    pair: Pair,
    timeSinceLastPaired: number | string
}


const MAX_SAFE_INTEGER = Number.MAX_SAFE_INTEGER || 9007199254740991;

export default class StatisticComposer {

    compose(tribe: Tribe, players: Player[], history: PairAssignmentDocument[]) {
        return {
            spinsUntilFullRotation: this.calculateFullRotation(players),
            pairReports: this.buildPairReports(players, history),
            medianSpinDuration: this.calculateMedianSpinDuration(history)
        };
    }

    private calculateMedianSpinDuration(history: PairAssignmentDocument[]) {
        if (history.length <= 1) {
            return 'N/A';
        }

        const times = history.map(document => parse(document.date).valueOf());
        const durations = times.slice(1).map((value, index) => times[index] - value);

        const sortedDurations = _.sortBy(durations, (duration) => duration);
        const indexOfMedian = Math.floor(sortedDurations.length / 2);
        const median = sortedDurations[indexOfMedian];

        return distanceInWords(0, median);
    }

    private buildPairReports(players: Player[], history) {
        return _.chain(players)
            .map(this.allPairsForPlayer)
            .flatten(true)
            .map((pair: Pair) => {
                return this.makeReport(pair, calculateTimeSinceLastPartnership(pair, history))
            })
            .sortBy((pairReport1: PairReport) => {

                if (pairReport1.timeSinceLastPaired === NEVER_PAIRED) {
                    return -1;
                }

                return MAX_SAFE_INTEGER - (pairReport1.timeSinceLastPaired as number);
            })
            .value();
    }

    private makeReport(pair, timeSinceLastPaired) {
        return ({
            pair: pair,
            timeSinceLastPaired: timeSinceLastPaired
        });
    }

    private allPairsForPlayer(player, index, players: Player[]) {
        const otherPlayers = players.slice(index + 1);
        return _.map(otherPlayers, otherPlayer => [player, otherPlayer]);
    }

    private calculateFullRotation(players: Player[]) {
        if (players.length % 2 === 0) {
            return players.length - 1;
        } else {
            return players.length;
        }
    }
}