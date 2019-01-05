import PairHeatCalculator from "../../../common/PairHeatCalculator";
import * as menuHistory from "./menu-history-bla.json";
import * as menuPlayers from "./menu-players.json";
import * as _ from "underscore";
import Pair from "../../../common/Pair";
import PairAssignmentSet from "../../../common/PairAssignmentSet";
import Player from "../../../common/Player";
import StatisticComposer from "../../../common/StatisticComposer";

fdescribe('srsly delete me', function () {

    const pairHeatCalculator = new PairHeatCalculator();
    const fullHistory: PairAssignmentSet[] = menuHistory;

    describe('menu', function () {

        it('what jennifer said', function () {
            const pair: Pair = [menuPlayers[0], menuPlayers[1]];
            console.log(_.pluck(pair, 'name'));

            const earlyHistory = fullHistory.slice(fullHistory.length - 90);

            const heat = pairHeatCalculator.calculate(pair, earlyHistory, 4);
            console.log('heat', heat);
        });

        it('what rob said', function () {
            const firstRotations = fullHistory.slice(fullHistory.length - 35);

            const uniquePlayers = _.chain(firstRotations)
                .map(interval => interval.pairs)
                .flatten(true)
                .flatten(true)
                .uniq(false, player => player._id)
                .value();

            const pairs = _.chain(uniquePlayers)
                .map((player, index, players :Player[]) => {
                    const otherPlayers = players.slice(index + 1);
                    return _.map(otherPlayers, otherPlayer => [player, otherPlayer]);
                })
                .flatten(true)
                .value();

            const stats = new StatisticComposer().compose({id: 'Roadkill Buffet', name: 'Menu'}, uniquePlayers, firstRotations);

            _.forEach(pairs, (pair :Pair) => {
                const pairNames = _.pluck(pair, 'name');
                const heat = pairHeatCalculator.calculate(pair, firstRotations, stats.spinsUntilFullRotation);
                console.log(pairNames, heat);
            })
        });

    });

});
