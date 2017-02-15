import StatisticComposer from "../../../server/lib/StatisticComposer";
import Player from "../../../common/Player";
import Tribe from "../../../common/Tribe";
import PairAssignmentDocument from "../../../common/PairAssignmentDocument";
import * as _ from "underscore";

const statComposer = new StatisticComposer();

describe('StatisticComposer', function () {

    let makePlayer = function (tribe: Tribe, id: string) {
        return {_id: id, tribe: tribe.id};
    };
    let makePlayers = function (tribe: Tribe, numberOfPlayers: number) {
        return _.map(Array.apply(null, {length: numberOfPlayers}),
            (value, index) => makePlayer(tribe, index.toString())
        );
    };

    describe('will include the full rotation number', function () {

        const tribe: Tribe = {id: 'LOL', name: 'LOL'};
        const history: PairAssignmentDocument[] = [];

        it('and it will be 1 with one player', function () {
            const players: Player[] = makePlayers(tribe, 1);
            const result = statComposer.compose(tribe, players, history);
            expect(result.spinsUntilFullRotation).toBe(1);
        });

        it('and it will be 1 with two players', function () {
            const players: Player[] = makePlayers(tribe, 2);
            const result = statComposer.compose(tribe, players, history);
            expect(result.spinsUntilFullRotation).toBe(1);
        });

        it('and it will be 3 with three players', function () {
            const players: Player[] = makePlayers(tribe, 3);
            const result = statComposer.compose(tribe, players, history);
            expect(result.spinsUntilFullRotation).toBe(3);
        });

        it('and it will be 3 with four players', function () {
            const players: Player[] = makePlayers(tribe, 4);
            const result = statComposer.compose(tribe, players, history);
            expect(result.spinsUntilFullRotation).toBe(3);
        });

        it('and it will be 7 with seven players', function () {
            const players: Player[] = makePlayers(tribe, 7);
            const result = statComposer.compose(tribe, players, history);
            expect(result.spinsUntilFullRotation).toBe(7);
        });

        it('and it will be 7 with eight players', function () {
            const players: Player[] = makePlayers(tribe, 8);
            const result = statComposer.compose(tribe, players, history);
            expect(result.spinsUntilFullRotation).toBe(7);
        });
    });

});