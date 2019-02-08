import PlayerHeatCalculator from "../app/runners/PlayerHeatCalculator";
import Player from "../../common/Player";
import PairAssignmentSet from "../../common/PairAssignmentSet";

describe('PlayerHeatCalculator', function () {

    it('with no players, returns no data', function () {
        const calculator = new PlayerHeatCalculator();
        const players = [];
        const heatMapValues = calculator.calculateHeatValues(players, [], 0);
        expect(heatMapValues).toEqual([]);
    });

    it('with one player, produces one row with a null', function () {
        const calculator = new PlayerHeatCalculator();
        const players: Player[] = [{_id: '0'}];
        const heatMapValues = calculator.calculateHeatValues(players, [], 0);
        expect(heatMapValues).toEqual([[null]]);
    });

    it('with three players and no history, produces three rows', function () {
        const calculator = new PlayerHeatCalculator();
        const players: Player[] = [
            {_id: '0'},
            {_id: '1'},
            {_id: '2'}
        ];
        const history: PairAssignmentSet[] = [];
        const heatMapValues = calculator.calculateHeatValues(players, history, 3);

        expect(heatMapValues).toEqual([
            [null, 0, 0],
            [0, null, 0],
            [0, 0, null]
        ]);
    });

    it('with two players and short history, produces two rows with heat values', function () {
        const calculator = new PlayerHeatCalculator();
        const players: Player[] = [
            {_id: '0'},
            {_id: '1'}
        ];
        const history: PairAssignmentSet[] = [{pairs: [[players[0], players[1]]], date: ''}];
        const rotationPeriod = 1;
        const heatMapValues = calculator.calculateHeatValues(players, history, rotationPeriod);

        expect(heatMapValues).toEqual([
            [null, 1],
            [1, null]
        ]);
    });

    it('with two players and full history, produces two rows with heat values', function () {
        const calculator = new PlayerHeatCalculator();
        const players: Player[] = [
            {_id: '0'},
            {_id: '1'}
        ];
        const history: PairAssignmentSet[] = [
            {pairs: [[players[0], players[1]]], date: ''},
            {pairs: [[players[0], players[1]]], date: ''},
            {pairs: [[players[0], players[1]]], date: ''},
            {pairs: [[players[0], players[1]]], date: ''},
            {pairs: [[players[0], players[1]]], date: ''},
        ];
        const rotationPeriod = 1;
        const heatMapValues = calculator.calculateHeatValues(players, history, rotationPeriod);

        expect(heatMapValues).toEqual([
            [null, 10],
            [10, null]
        ]);
    });

    it('with three players and interesting history, produces three rows with heat values', function () {
        const calculator = new PlayerHeatCalculator();
        const players: Player[] = [
            {_id: '0'},
            {_id: '1'},
            {_id: '2'},
        ];

        const history: PairAssignmentSet[] = [
            {pairs: [[players[0], players[1]]], date: ''},
            {pairs: [[players[0], players[1]]], date: ''},
            {pairs: [[players[0], players[1]]], date: ''},
            {pairs: [[players[0], players[1]]], date: ''},
            {pairs: [[players[0], players[1]]], date: ''},
            {pairs: [[players[0], players[1]]], date: ''},
            {pairs: [[players[0], players[1]]], date: ''},
            {pairs: [[players[0], players[1]]], date: ''},
            {pairs: [[players[0], players[1]]], date: ''},
            {pairs: [[players[0], players[1]]], date: ''},
            {pairs: [[players[0], players[1]]], date: ''},
            {pairs: [[players[0], players[1]]], date: ''},
            {pairs: [[players[0], players[1]]], date: ''},
            {pairs: [[players[0], players[1]]], date: ''},
            {pairs: [[players[0], players[2]]], date: ''},
        ];
        const rotationPeriod = 3;
        const heatMapValues = calculator.calculateHeatValues(players, history, rotationPeriod);

        expect(heatMapValues).toEqual([
            [null, 10, 1],
            [10, null, 0],
            [1, 0, null],
        ]);
    });

});