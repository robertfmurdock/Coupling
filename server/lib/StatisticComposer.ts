import Tribe from "../../common/Tribe";
import Player from "../../common/Player";
import PairAssignmentDocument from "../../common/PairAssignmentDocument";

export default class StatisticComposer {

    compose(tribe: Tribe, players: Player[], history: PairAssignmentDocument[]) {
        return {
            spinsUntilFullRotation: this.calculateFullRotation(players)
        };
    }

    private calculateFullRotation(players: Player[]) {
        if(players.length % 2 === 0) {
            return players.length - 1;
        } else {
            return players.length;
        }
    }
}