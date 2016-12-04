import PairAssignmentDocument from "../../common/PairAssignmentDocument";
import PinAssigner from "./PinAssigner";
var clock = require('./Clock.ts');

export default class GameRunner {
    constructor(public gameFactory) {
    }

    public run(players, pins, history) {
        var game = this.gameFactory.buildGame(history);

        new PinAssigner().assignPins(pins, players);
        var pairs = game.play(players);

        return new PairAssignmentDocument(clock.getDate(), pairs);
    };
};