import * as React from "react";
import map from "ramda/es/map";
import {shallow} from 'enzyme';
import PlayerHeatmap from "../app/components/statistics/PlayersHeatmap";
import * as Styles from "../app/components/statistics/styles.css";
import {Tribe, Player} from "../../common";

describe('PlayersHeatmap', function () {

    function buildWrapper(tribe: Tribe, players: Player[], heatmapData) {
        return shallow(<PlayerHeatmap heatmapData={{heatmapData}} tribe={tribe} players={players}/>);
    }

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

});