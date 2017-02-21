import Tribe from "../../common/Tribe";
import Player from "../../common/Player";
import PairAssignmentDocument from "../../common/PairAssignmentDocument";
import * as _ from "underscore";
import {calculateTimeSinceLastPartnership, NEVER_PAIRED} from "../../common/PairingTimeCalculator";
import Pair from "../../common/Pair";

interface PairReport {
    pair: Pair,
    timeSinceLastPaired: number | string
}

export default class StatisticComposer {

    compose(tribe: Tribe, players: Player[], history: PairAssignmentDocument[]) {
        return {
            spinsUntilFullRotation: this.calculateFullRotation(players),
            pairReports: this.buildPairReports(players, history)
        };
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

                return Number.MAX_SAFE_INTEGER - (pairReport1.timeSinceLastPaired as number);
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