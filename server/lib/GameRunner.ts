import Player from "../../common/Player";

// @ts-ignore
import {spinContext} from "engine";
import Comparators from "../../common/Comparators";

const context = spinContext({areEqualPairs: Comparators.areEqualPairsSyntax});

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