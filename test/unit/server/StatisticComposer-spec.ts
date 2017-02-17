import StatisticComposer from "../../../server/lib/StatisticComposer";
import Player from "../../../common/Player";
import Tribe from "../../../common/Tribe";
import PairAssignmentDocument from "../../../common/PairAssignmentDocument";
import * as _ from "underscore";
import Pair from "../../../common/Pair";
import {NEVER_PAIRED} from "../../../common/PairingTimeCalculator";

const statComposer = new StatisticComposer();

describe('StatisticComposer', function () {

    let makePlayer = function (tribe: Tribe, id: string) {
        return {_id: id, tribe: tribe.id};
    };

    let makePlayers = function (tribe: Tribe, numberOfPlayers: number) {
        return _.map(Array.apply(null, {length: numberOfPlayers}),
            (value, index) => makePlayer(tribe, (index + 1).toString())
        );
    };

    describe('will include the full rotation number', function () {

        const tribe: Tribe = {id: 'LOL', name: 'LOL'};
        const history: PairAssignmentDocument[] = [];

        it('and it will be 1 with one player', function () {
            const players: Player[] = makePlayers(tribe, 1);
            const {spinsUntilFullRotation} = statComposer.compose(tribe, players, history);
            expect(spinsUntilFullRotation).toBe(1);
        });

        it('and it will be 1 with two players', function () {
            const players: Player[] = makePlayers(tribe, 2);
            const {spinsUntilFullRotation} = statComposer.compose(tribe, players, history);
            expect(spinsUntilFullRotation).toBe(1);
        });

        it('and it will be 3 with three players', function () {
            const players: Player[] = makePlayers(tribe, 3);
            const {spinsUntilFullRotation} = statComposer.compose(tribe, players, history);
            expect(spinsUntilFullRotation).toBe(3);
        });

        it('and it will be 3 with four players', function () {
            const players: Player[] = makePlayers(tribe, 4);
            const {spinsUntilFullRotation} = statComposer.compose(tribe, players, history);
            expect(spinsUntilFullRotation).toBe(3);
        });

        it('and it will be 7 with seven players', function () {
            const players: Player[] = makePlayers(tribe, 7);
            const {spinsUntilFullRotation} = statComposer.compose(tribe, players, history);
            expect(spinsUntilFullRotation).toBe(7);
        });

        it('and it will be 7 with eight players', function () {
            const players: Player[] = makePlayers(tribe, 8);
            const {spinsUntilFullRotation} = statComposer.compose(tribe, players, history);
            expect(spinsUntilFullRotation).toBe(7);
        });
    });

    describe('will generate pair reports', function () {

        it('with no players, no pair reports will be created', function () {
            const tribe: Tribe = {id: 'LOL', name: 'LOL'};
            const history: PairAssignmentDocument[] = [];

            const players: Player[] = [];
            const {pairReports} = statComposer.compose(tribe, players, history);
            expect(pairReports.length).toBe(0);
        });

        it('with one player and no history, no pair report will be created', function () {
            const tribe: Tribe = {id: 'LOL', name: 'LOL'};
            const history: PairAssignmentDocument[] = [];

            const players: Player[] = makePlayers(tribe, 1);
            const {pairReports} = statComposer.compose(tribe, players, history);
            expect(pairReports).toEqual([]);
        });

        it('with two players and no history, one pair report will be created', function () {
            const tribe: Tribe = {id: 'LOL', name: 'LOL'};
            const history: PairAssignmentDocument[] = [];

            const players: Player[] = makePlayers(tribe, 2);
            const [player1, player2] = players;
            const {pairReports} = statComposer.compose(tribe, players, history);
            expect(pairReports.length).toBe(1);
            const pair = pairReports[0].pair;
            expect(pair).toEqual([player1, player2]);
        });

        it('with five players and no history, one pair report will be created', function () {
            const tribe: Tribe = {id: 'LOL', name: 'LOL'};
            const history: PairAssignmentDocument[] = [];

            const players: Player[] = makePlayers(tribe, 5);
            const [player1, player2, player3, player4, player5] = players;
            const {pairReports} = statComposer.compose(tribe, players, history);

            checkPairs(_.pluck(pairReports, 'pair'), [
                [player1, player2],
                [player1, player3],
                [player1, player4],
                [player1, player5],
                [player2, player3],
                [player2, player4],
                [player2, player5],
                [player3, player4],
                [player3, player5],
                [player4, player5],
            ]);
        });

        function checkPairs(actualPairs: any[], expected: Pair[]) {
            function reportResults() {
                const actualFormattedValue = JSON.stringify(_.chain(actualPairs)
                    .map(pair => _.pluck(pair, '_id'))
                    .value());

                const expectedFormattedValue = JSON.stringify(_.chain(expected)
                    .map(pair => _.pluck(pair, '_id'))
                    .value());
                return `\n----------WE EXPECT\n${expectedFormattedValue}\n----------RESULTS\n${actualFormattedValue}\n`
            }

            expect(actualPairs).toEqual(expected, reportResults());
        }

        it('with four players, pair reports are ordered by longest time since last pairing', function () {
            const tribe: Tribe = {id: 'LOL', name: 'LOL'};
            const players: Player[] = makePlayers(tribe, 4);
            const [player1, player2, player3, player4] = players;

            const history: PairAssignmentDocument[] = [
                new PairAssignmentDocument('', [
                    [player1, player3], [player2, player4],
                ], tribe.id),
                new PairAssignmentDocument('', [
                    [player1, player2], [player3, player4],
                ], tribe.id)
            ];

            const {pairReports} = statComposer.compose(tribe, players, history);
            checkPairs(_.pluck(pairReports, 'pair'), [
                [player1, player4],
                [player2, player3],
                [player1, player2],
                [player3, player4],
                [player1, player3],
                [player2, player4],
            ]);

            expect(_.pluck(pairReports, 'timeSinceLastPaired')).toEqual([
                NEVER_PAIRED,
                NEVER_PAIRED,
                1,
                1,
                0,
                0,
            ]);
        });

    });

});