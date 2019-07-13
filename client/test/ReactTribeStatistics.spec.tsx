import * as React from "react";
import {shallow} from 'enzyme';
import times from 'ramda/es/times'
import map from "ramda/es/map";
import Tribe from "../../common/Tribe";
import Player from "../../common/Player";
import PairAssignmentSet from "../../common/PairAssignmentSet";
import {NEVER_PAIRED} from "../../common/PairingTimeCalculator";
import ReactTribeStatistics, {PairReportTable, TeamStatistics} from "../app/components/statistics/ReactTribeStatistics";
import PlayerHeatmap from "../app/components/statistics/PlayersHeatmap";
import TribeCard from "../app/components/tribe-card/ReactTribeCard";

describe('ReactTribeStatistics', function () {

    function buildWrapper(tribe: Tribe, players: Player[], history: PairAssignmentSet[]) {
        return shallow(<ReactTribeStatistics
            tribe={tribe} players={players} history={history} pathSetter={() => undefined}
        />);
    }

    it('will show a tribe card', function () {
        const tribe: Tribe = {id: '1', name: 'Super'};
        const wrapper = buildWrapper(tribe, [], []);
        const tribeCard = wrapper.find(TribeCard);
        expect(tribeCard.props().tribe).toBe(tribe);
    });

    describe('will show pairings', function () {

        beforeAll(function () {
            this.tribe = {id: '2', name: 'Mathematica'};
            this.players = [
                {_id: 'harry', name: 'Harry', tribe: '2'},
                {_id: 'larry', name: 'Larry', tribe: '2'},
                {_id: 'curly', name: 'Curly', tribe: '2'},
                {_id: 'moe', name: 'Moe', tribe: '2'}
            ];

            this.history = [{
                pairs: [[this.players[0], this.players[1]], [this.players[2], this.players[3]]],
                date: '',
                tribe: this.tribe.id
            }];
        });

        it('ordered by longest time since last paired', function () {
            this.wrapper = buildWrapper(this.tribe, this.players, this.history);
            const pairElements = this.wrapper.find(PairReportTable);
            let pairReports = pairElements.props().pairReports;
            let numberOfElements = pairReports.length;

            const actualPairedPlayerNames = times((index) => {

                let children = pairReports[index].pair;

                return [
                    children[0].name,
                    children[1].name
                ];
            }, numberOfElements);

            expect(actualPairedPlayerNames).toEqual([
                ['Harry', 'Curly'],
                ['Harry', 'Moe'],
                ['Larry', 'Curly'],
                ['Larry', 'Moe'],
                ['Harry', 'Larry'],
                ['Curly', 'Moe'],
            ]);
        });

        it('with the time since that pair last occurred', function () {
            this.wrapper = buildWrapper(this.tribe, this.players, this.history);
            const pairReportTableWrapper = this.wrapper.find(PairReportTable);
            let pairReports = pairReportTableWrapper.props().pairReports;
            const timeValues = pairReports.map(report => report.timeSinceLastPaired);
            expect(timeValues).toEqual([
                NEVER_PAIRED,
                NEVER_PAIRED,
                NEVER_PAIRED,
                NEVER_PAIRED,
                0,
                0
            ]);
        });
    });

    it('sends player heat data to subdirective', function () {
        this.tribe = {id: '2', name: 'Mathematica'};
        this.players = [
            {_id: 'harry', name: 'Harry', tribe: '2'},
            {_id: 'larry', name: 'Larry', tribe: '2'},
            {_id: 'curly', name: 'Curly', tribe: '2'},
            {_id: 'moe', name: 'Moe', tribe: '2'}
        ];

        this.history = [{
            pairs: [[this.players[0], this.players[1]], [this.players[2], this.players[3]]],
            date: '',
            tribe: this.tribe.id
        }];

        this.wrapper = buildWrapper(this.tribe, this.players, this.history);
        const heatmapElement = this.wrapper.find(PlayerHeatmap);

        expect(heatmapElement.props().heatmapData).toEqual([
            [null, 1, 0, 0],
            [1, null, 0, 0],
            [0, 0, null, 1],
            [0, 0, 1, null]
        ]);
    });

    it('will show the current number of active players', function () {
        this.tribe = {id: '2', name: 'Mathematica'};
        this.players = [
            {_id: 'harry', name: 'Harry', tribe: '2'},
            {_id: 'larry', name: 'Larry', tribe: '2'},
            {_id: 'curly', name: 'Curly', tribe: '2'},
            {_id: 'moe', name: 'Moe', tribe: '2'}
        ];

        this.wrapper = buildWrapper(this.tribe, this.players, []);

        const teamStatisticsWrapper = this.wrapper.find(TeamStatistics);
        expect(teamStatisticsWrapper.props().activePlayerCount).toBe(4);
    });

    it('will show the rotation number', function () {
        const tribe: Tribe = {id: '2', name: 'Mathematica'};
        const players: Player[] = [
            {_id: 'harry'},
            {_id: 'larry'},
            {_id: 'curly'},
            {_id: 'moe'}
        ];

        const wrapper = buildWrapper(tribe, players, []);
        const teamStatisticsWrapper = wrapper.find(TeamStatistics);
        expect(teamStatisticsWrapper.props().spinsUntilFullRotation).toBe(3);
    });

    it('will show the median spin time', function () {
        this.tribe = {id: '2', name: 'Mathematica'};
        this.players = [
            {_id: 'harry', name: 'Harry', tribe: '2'},
            {_id: 'larry', name: 'Larry', tribe: '2'},
            {_id: 'curly', name: 'Curly', tribe: '2'},
            {_id: 'moe', name: 'Moe', tribe: '2'}
        ];

        this.history = [
            {
                pairs: [[this.players[0], this.players[1]], [this.players[2], this.players[3]]],
                date: new Date(2017, 3, 14),
                tribe: this.tribe.id
            },
            {
                pairs: [[this.players[0], this.players[1]], [this.players[2], this.players[3]]],
                date: new Date(2017, 3, 12),
                tribe: this.tribe.id
            },
        ];
        const wrapper = buildWrapper(this.tribe, this.players, this.history);
        const teamStatisticsWrapper = wrapper.find(TeamStatistics);
        expect(teamStatisticsWrapper.props().medianSpinDuration).toBe('2 days');
    });
});
