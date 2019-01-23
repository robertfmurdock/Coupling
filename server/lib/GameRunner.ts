import Player from "../../common/Player";

// @ts-ignore
import {spinContext} from "engine";

const context = spinContext();

export default class GameRunner {
    constructor(public commandDispatcher = context) {
    }

    public run(players: Player[], pins, history, tribe) {
        return this.commandDispatcher.performRunGameCommand(
            history,
            players,
            pins,
            tribe
        );
    };
};