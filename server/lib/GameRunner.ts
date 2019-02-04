import Player from "../../common/Player";

// @ts-ignore
import {spinContext, performRunGameCommand} from "engine";

const context = spinContext();

export default class GameRunner {
    constructor(public commandDispatcher = context) {
    }

    public run(players: Player[], pins, history, tribe) {
        return performRunGameCommand(
            this.commandDispatcher,
            history,
            players,
            pins,
            tribe
        );
    };
};