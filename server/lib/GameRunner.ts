import PairAssignmentDocument from "../../common/PairAssignmentDocument";
import PinAssigner from "./PinAssigner";
import * as clock from "./Clock";
import Player from "../../common/Player";

export default class GameRunner {
    constructor(public gameFactory) {
    }

    public run(players : Player[], pins, history, tribe) {
        const game = this.gameFactory.buildGame(history);

        new PinAssigner().assignPins(pins, players);
        const pairs = game.play(players, tribe.pairingRule);

        return new PairAssignmentDocument(clock.getDate(), pairs, tribe.id);
    };
};