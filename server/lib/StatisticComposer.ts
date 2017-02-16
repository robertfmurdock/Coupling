import Tribe from "../../common/Tribe";
import Player from "../../common/Player";
import PairAssignmentDocument from "../../common/PairAssignmentDocument";
import * as _ from "underscore";
import {calculateTimeSinceLastPartnership, NEVER_PAIRED} from "./PairingHistory";

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
            .map(pair => {
                return this.makeReport(pair)
            })
            .sort((pairReport1, pairReport2) => {
                const timeSincePair1LastPaired = calculateTimeSinceLastPartnership(pairReport1.pair, history);
                const timeSincePair2LastPaired = calculateTimeSinceLastPartnership(pairReport2.pair, history);

                if(timeSincePair1LastPaired === NEVER_PAIRED) {
                    return false;
                }
                if(timeSincePair2LastPaired === NEVER_PAIRED) {
                    return true;
                }

                return timeSincePair1LastPaired < timeSincePair2LastPaired;

            })
            .value();
    }

    private makeReport(pair) {
        return ({
            pair: pair
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