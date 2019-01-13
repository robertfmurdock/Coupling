import PairAssignmentDocument from "../../common/PairAssignmentDocument";
import PinAssigner from "./PinAssigner";
import * as clock from "./Clock";
import Player from "../../common/Player";

// @ts-ignore
import {SpinCommandDispatcher, historyFromArray, PairingRule, spinContext} from "engine";
import Comparators from "../../common/Comparators";

const context = spinContext({areEqualPairs: Comparators.areEqualPairsSyntax});

export default class GameRunner {
    constructor(public commandDispatcher = context) {
    }

    public run(players: Player[], pins, history, tribe) {
        const pairs = this.commandDispatcher.runSpinCommand(
            historyFromArray(history),
            players,
            PairingRule.Companion.fromValue(tribe.pairingRule)
        );

        new PinAssigner().assignPins(pins, players);

        return new PairAssignmentDocument(clock.getDate(), pairs, tribe.id);
    };
};