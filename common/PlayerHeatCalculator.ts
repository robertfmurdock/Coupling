import Player from "./Player";
import * as R from "ramda";
import PairAssignmentSet from "./PairAssignmentSet";
import PairHeatCalculator from "./PairHeatCalculator";

const pairHeatCalculator = new PairHeatCalculator();

export default class PlayerHeatCalculator {

    calculateHeatValues(players: Array<Player>, history: PairAssignmentSet[], rotationPeriod) {
        const createRow = R.map((player: Player) => {
            return R.map(this.toHeatValuesForPlayer(player, history, rotationPeriod), players);
        });
        return createRow(players);
    }

    private toHeatValuesForPlayer(player: Player, history: PairAssignmentSet[], rotationPeriod) {
        return (alternatePlayer: Player) => {
            if (alternatePlayer === player) {
                return null;
            } else {
                return pairHeatCalculator.calculate([player, alternatePlayer], history, rotationPeriod);
            }
        };
    }
}