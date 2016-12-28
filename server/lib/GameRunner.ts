import PairAssignmentDocument from "../../common/PairAssignmentDocument";
import PinAssigner from "./PinAssigner";
import * as clock from "./Clock";

export default class GameRunner {
    constructor(public gameFactory) {
    }

    public run(players, pins, history, tribeId) {
        const game = this.gameFactory.buildGame(history);

        new PinAssigner().assignPins(pins, players);
        const pairs = game.play(players);

        return new PairAssignmentDocument(clock.getDate(), pairs, tribeId);
    };
};