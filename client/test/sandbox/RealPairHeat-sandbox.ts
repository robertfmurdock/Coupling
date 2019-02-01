import PairHeatCalculator from "../../app/runners/PairHeatCalculator";
import * as menuHistory from "./menu-history-bla.json";
import * as menuPlayers from "./menu-players.json";
import Pair from "../../../common/Pair";
import PairAssignmentSet from "../../../common/PairAssignmentSet";
import Player from "../../../common/Player";
import StatisticComposer from "../../app/runners/StatisticComposer";
import * as pluck from 'ramda/src/pluck'
import * as pipe from 'ramda/src/pipe'
import * as map from 'ramda/src/map'
import * as unnest from 'ramda/src/unnest'
import * as uniq from 'ramda/src/uniq'
import * as forEach from 'ramda/src/forEach'

fdescribe('srsly delete me', function () {

    const pairHeatCalculator = new PairHeatCalculator();
    const fullHistory: PairAssignmentSet[] = menuHistory;

    describe('menu', function () {

        it('what jennifer said', function () {
            const pair: Pair = [menuPlayers[0], menuPlayers[1]];
            console.log(pluck('name', pair));

            const earlyHistory = fullHistory.slice(fullHistory.length - 90);

            const heat = pairHeatCalculator.calculate(pair, earlyHistory, 4);
            console.log('heat', heat);
        });

        it('what rob said', function () {
            const firstRotations = fullHistory.slice(fullHistory.length - 35);

            const uniquePlayers = pipe(
                map(interval => interval.pairs),
                unnest(),
                unnest(),
                uniq(player => player._id)
            )(firstRotations);

            const pairs = pipe(
                map((player, index, players: Player[]) => {
                    const otherPlayers = players.slice(index + 1);
                    return map(otherPlayer => [player, otherPlayer], otherPlayers);
                }),
                unnest())(uniquePlayers);

            const stats = new StatisticComposer().compose({
                id: 'Roadkill Buffet',
                name: 'Menu'
            }, uniquePlayers, firstRotations);

            forEach((pair: Pair) => {
                const pairNames = pluck('name', pair);
                const heat = pairHeatCalculator.calculate(pair, firstRotations, stats.spinsUntilFullRotation);
                console.log(pairNames, heat);
            }, pairs)
        });

    });

});
