import Tribe from "../../common/Tribe";
import Player from "../../common/Player";
import PairAssignmentDocument from "../../common/PairAssignmentDocument";
import * as _ from "underscore";
import {calculateTimeSinceLastPartnership, NEVER_PAIRED} from "../../common/PairingTimeCalculator";
import Pair from "../../common/Pair";

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
            .sort((pairReport1, pairReport2) => {
                if(pairReport1.timeSinceLastPaired === NEVER_PAIRED) {
                    return false;
                }
                if(pairReport2.timeSinceLastPaired === NEVER_PAIRED) {
                    return true;
                }

                return pairReport1.timeSinceLastPaired < pairReport2.timeSinceLastPaired;
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