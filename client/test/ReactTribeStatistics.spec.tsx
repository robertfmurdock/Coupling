import * as angular from "angular";
import "ng-fittext";
import "../app/app";
import Tribe from "../../common/Tribe";
import {shallow} from 'enzyme';
import times from 'ramda/es/times'
import Player from "../../common/Player";
import map from "ramda/es/map";
import PairAssignmentSet from "../../common/PairAssignmentSet";
import * as Styles from "../../client/app/components/statistics/styles.css";
import {NEVER_PAIRED} from "../../common/PairingTimeCalculator";
import ReactTribeStatistics from "../app/components/statistics/ReactTribeStatistics";
import * as React from "react";

describe('ReactTribeStatistics', function () {

    function buildWrapper(tribe: Tribe, players: Player[], history: PairAssignmentSet[]) {
        return shallow(<ReactTribeStatistics
            tribe={tribe} players={players} history={history} pathSetter={() => undefined}
        />);
    }

    it('will show a tribe card', function () {
        const tribe: Tribe = {id: '1', name: 'Super'};
        const wrapper = buildWrapper(tribe, [], []);
        const tribeCard = wrapper.find(`ReactTribeCard`);
        expect(tribeCard.props().tribe).toBe(tribe);
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
        const rotationNumberElement = wrapper.find('.rotation-number');
        expect(rotationNumberElement.text()).toBe('3');
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
            const pairElements = this.wrapper.find(`.${Styles.pairReport}`);
            let numberOfElements = pairElements.length;

            const actualPairedPlayerNames = times((index) => {

                let children = pairElements.at(index)
                    .find(`.${Styles.pairReport} ReactPlayerCard`);

                return [
                    children.at(0).props().player.name,
                    children.at(1).props().player.name
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
            const timeElements = this.wrapper.find(`.${Styles.pairReport} .time-since-last-pairing`);

            const timeValues = times(index => timeElements.at(index).text(), timeElements.length);

            expect(timeValues).toEqual([
                NEVER_PAIRED,
                NEVER_PAIRED,
                NEVER_PAIRED,
                NEVER_PAIRED,
                '0',
                '0'
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
        const heatmapElement = this.wrapper.find("ReactHeatmap");

        expect(heatmapElement.props().data).toEqual([
            [null, 1, 0, 0],
            [1, null, 0, 0],
            [0, 0, null, 1],
            [0, 0, 1, null]
        ]);
    });

    const getPlayersFromCards = map(playerCardWrapper => playerCardWrapper.props().player);

    it('has row of players above heatmap', function () {
        this.tribe = {id: '2', name: 'Mathematica'};
        this.players = [
            {_id: 'harry', name: 'Harry', tribe: '2'},
            {_id: 'larry', name: 'Larry', tribe: '2'},
            {_id: 'curly', name: 'Curly', tribe: '2'},
            {_id: 'moe', name: 'Moe', tribe: '2'}
        ];

        this.wrapper = buildWrapper(this.tribe, this.players, []);
        const playersRowElement = this.wrapper.find(`.${Styles.heatmapPlayersTopRow}`);
        const playerCards = playersRowElement.find('ReactPlayerCard');

        const playersOnCards = getPlayersFromCards(playerCards);

        expect(playersOnCards).toEqual(this.players);
    });

    it('has a row of players to the side of the heatmap', function () {
        this.tribe = {id: '2', name: 'Mathematica'};
        this.players = [
            {_id: 'harry', name: 'Harry', tribe: '2'},
            {_id: 'larry', name: 'Larry', tribe: '2'},
            {_id: 'curly', name: 'Curly', tribe: '2'},
            {_id: 'moe', name: 'Moe', tribe: '2'}
        ];

        this.wrapper = buildWrapper(this.tribe, this.players, []);
        const playersRowElement = this.wrapper.find(`.${Styles.heatmapPlayersSideRow}`);
        const playerCards = playersRowElement.find('ReactPlayerCard');

        const playersOnCards = getPlayersFromCards(playerCards);

        expect(playersOnCards).toEqual(this.players);
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
        const activePlayerCountElement = this.wrapper.find(`.${Styles.activePlayerCount}`);

        expect(activePlayerCountElement.text()).toEqual('4');
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

        this.wrapper = buildWrapper(this.tribe, this.players, this.history);
        const medianSpinDurationElement = this.wrapper.find(`.${Styles.medianSpinDuration}`);

        expect(medianSpinDurationElement.text()).toEqual('2 days');
    });

});