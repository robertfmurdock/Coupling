import Player from "../../common/Player";
import Tribe from "../../common/Tribe";
import PairAssignmentDocument from "../../common/PairAssignmentDocument";
import Pair from "../../common/Pair";
import {NEVER_PAIRED} from "../../common/PairingTimeCalculator";
import StatisticComposer from "../../common/StatisticComposer";
import * as map from 'ramda/src/map'
import * as pluck from 'ramda/src/pluck'
import * as addIndex from 'ramda/src/addIndex'

const statComposer = new StatisticComposer();

describe('StatisticComposer', function () {

    let makePlayer = function (tribe: Tribe, id: string) {
        return {_id: id, tribe: tribe.id};
    };

    let makePlayers = function (tribe: Tribe, numberOfPlayers: number) {
        let mapToNewPlayer = addIndex(map)((value, index) => makePlayer(tribe, (index + 1).toString()));
        return mapToNewPlayer(Array.apply(null, {length: numberOfPlayers}));
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

            checkPairs(pluck('pair', pairReports), [
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
                const actualFormattedValue = JSON.stringify(map(pair => pluck('_id', pair), actualPairs));

                const expectedFormattedValue = JSON.stringify(map(pair => pluck('_id', pair))(expected));
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
            checkPairs(pluck('pair', pairReports), [
                [player1, player4],
                [player2, player3],
                [player1, player2],
                [player3, player4],
                [player1, player3],
                [player2, player4],
            ]);

            expect(pluck('timeSinceLastPaired', pairReports)).toEqual([
                NEVER_PAIRED,
                NEVER_PAIRED,
                1,
                1,
                0,
                0,
            ]);
        });

        it('still sorts correctly with large realistic history', function () {
            const {tribe, players, history} = require('./realistics-sort-test-data/inputs.json');
            const {pairReports} = statComposer.compose(tribe, players, history);
            const expectedResults = require('./realistics-sort-test-data/expectResults.json');
            expect(pluck('timeSinceLastPaired', pairReports)).toEqual(expectedResults);
        });

    });

    describe('will calculate the median spin time', function () {

        it('as N/A if no history', function () {
            const tribe: Tribe = {id: 'LOL', name: 'LOL'};
            const history: PairAssignmentDocument[] = [];
            const players: Player[] = [];

            const {medianSpinDuration} = statComposer.compose(tribe, players, history);
            expect(medianSpinDuration).toBe('N/A');
        });

        it('as 1 with daily spins', function () {
            const tribe: Tribe = {id: 'LOL', name: 'LOL'};
            const players: Player[] = [];

            const history: PairAssignmentDocument[] = [
                {date: new Date(2017, 2, 17), pairs: [], tribe: tribe.id},
                {date: new Date(2017, 2, 16), pairs: [], tribe: tribe.id},
                {date: new Date(2017, 2, 15), pairs: [], tribe: tribe.id},
                {date: new Date(2017, 2, 14), pairs: [], tribe: tribe.id},
                {date: new Date(2017, 2, 13), pairs: [], tribe: tribe.id},
                {date: new Date(2017, 2, 12), pairs: [], tribe: tribe.id},
            ];

            const {medianSpinDuration} = statComposer.compose(tribe, players, history);
            expect(medianSpinDuration).toBe('1 day');
        });

        it('as 2 with mostly 2 day spins and outliers', function () {
            const tribe: Tribe = {id: 'LOL', name: 'LOL'};
            const players: Player[] = [];

            const history: PairAssignmentDocument[] = [
                {date: new Date(2017, 2, 17), pairs: [], tribe: tribe.id},
                {date: new Date(2017, 2, 12), pairs: [], tribe: tribe.id},
                {date: new Date(2017, 2, 10), pairs: [], tribe: tribe.id},
                {date: new Date(2017, 2, 8), pairs: [], tribe: tribe.id},
                {date: new Date(2017, 2, 6), pairs: [], tribe: tribe.id},
                {date: new Date(2017, 2, 4), pairs: [], tribe: tribe.id},
                {date: new Date(2017, 2, 3), pairs: [], tribe: tribe.id},
            ];

            const {medianSpinDuration} = statComposer.compose(tribe, players, history);
            expect(medianSpinDuration).toBe('2 days');
        });

        it('with dates as string', function () {
            const tribe: Tribe = {id: 'LOL', name: 'LOL'};
            const players: Player[] = [];

            const history: PairAssignmentDocument[] = [
                {date: '2017-02-21T16:34:35.173Z', pairs: [], tribe: tribe.id},
                {date: '2017-02-17T16:34:35.173Z', pairs: [], tribe: tribe.id}
            ];

            const {medianSpinDuration} = statComposer.compose(tribe, players, history);
            expect(medianSpinDuration).toBe('4 days');
        });

        it('with one instance of median and variable pattern', function () {
            const tribe: Tribe = {id: 'LOL', name: 'LOL'};
            const players: Player[] = [];

            const history: PairAssignmentDocument[] = [
                {date: new Date(2017, 2, 20), pairs: [], tribe: tribe.id},
                {date: new Date(2017, 2, 17), pairs: [], tribe: tribe.id},
                {date: new Date(2017, 2, 15), pairs: [], tribe: tribe.id},
                {date: new Date(2017, 2, 14), pairs: [], tribe: tribe.id},
                {date: new Date(2017, 2, 13), pairs: [], tribe: tribe.id},
                {date: new Date(2017, 2, 10), pairs: [], tribe: tribe.id},
            ];

            const {medianSpinDuration} = statComposer.compose(tribe, players, history);
            expect(medianSpinDuration).toBe('2 days');
        });

        it('as N/A with only one history entry', function () {
            const tribe: Tribe = {id: 'LOL', name: 'LOL'};
            const players: Player[] = [];

            const history: PairAssignmentDocument[] = [{date: new Date(2017, 2, 17), pairs: [], tribe: tribe.id}];

            const {medianSpinDuration} = statComposer.compose(tribe, players, history);
            expect(medianSpinDuration).toBe('N/A');
        });

        it('down to the hour!', function () {
            const tribe: Tribe = {id: 'LOL', name: 'LOL'};
            const players: Player[] = [];

            const history: PairAssignmentDocument[] = [
                {date: new Date(2017, 2, 20, 21), pairs: [], tribe: tribe.id},
                {date: new Date(2017, 2, 20, 19), pairs: [], tribe: tribe.id},
                {date: new Date(2017, 2, 20, 18), pairs: [], tribe: tribe.id},
                {date: new Date(2017, 2, 20, 13), pairs: [], tribe: tribe.id},
                {date: new Date(2017, 2, 20, 12), pairs: [], tribe: tribe.id},
                {date: new Date(2017, 2, 20, 9), pairs: [], tribe: tribe.id},
            ];

            const {medianSpinDuration} = statComposer.compose(tribe, players, history);
            expect(medianSpinDuration).toBe('about 2 hours');
        });

        it('rounding hours to nearest day when the median is greater than a day', function () {
            const tribe: Tribe = {id: 'LOL', name: 'LOL'};
            const players: Player[] = [];

            const history: PairAssignmentDocument[] = [
                {date: new Date(2017, 2, 20, 21), pairs: [], tribe: tribe.id},
                {date: new Date(2017, 2, 17, 19), pairs: [], tribe: tribe.id},
                {date: new Date(2017, 2, 15, 7), pairs: [], tribe: tribe.id},
                {date: new Date(2017, 2, 14, 13), pairs: [], tribe: tribe.id},
                {date: new Date(2017, 2, 13, 12), pairs: [], tribe: tribe.id},
                {date: new Date(2017, 2, 10, 9), pairs: [], tribe: tribe.id},
            ];

            const {medianSpinDuration} = statComposer.compose(tribe, players, history);
            expect(medianSpinDuration).toBe('3 days');
        });
    });
});