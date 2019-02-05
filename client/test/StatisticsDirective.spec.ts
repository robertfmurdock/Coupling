import * as angular from "angular";
import "ng-fittext";
import "../app/app";
import Tribe from "../../common/Tribe";
import * as times from 'ramda/src/times'
import Player from "../../common/Player";
import PairAssignmentSet from "../../common/PairAssignmentSet";
import * as Styles from "../../client/app/components/statistics/styles.css";
import * as map from "ramda/src/map";
import {NEVER_PAIRED} from "../../common/PairingTimeCalculator";

const tribeCardStyles = require('../../client/app/components/tribe-card/styles.css');

describe('Statistics directive', function () {

    beforeEach(angular.mock.module('coupling'));

    function buildDirective($rootScope, $compile: angular.ICompileService, tribe: Tribe, players: Player[], history: PairAssignmentSet[]) {
        const element = angular.element('<statistics tribe="tribe" players="players" history="history"/>');
        const scope = $rootScope.$new();
        scope.tribe = tribe;
        scope.players = players;
        scope.history = history;
        const statisticsDirective = $compile(element)(scope);

        scope.$digest();
        return statisticsDirective;
    }

    it('will show a tribe card', inject(function ($compile, $rootScope) {
        const tribe: Tribe = {id: '1', name: 'Super'};
        const statisticsDirective = buildDirective($rootScope, $compile, tribe, [], []);
        const tribeNameElement = statisticsDirective.find(`.${tribeCardStyles.header}`);

        expect(tribeNameElement.text()).toBe(tribe.name);
    }));

    it('will show the rotation number', inject(function ($compile, $rootScope) {
        const tribe: Tribe = {id: '2', name: 'Mathematica'};
        const players: Player[] = [
            {_id: 'harry'},
            {_id: 'larry'},
            {_id: 'curly'},
            {_id: 'moe'}
        ];

        const statisticsDirective = buildDirective($rootScope, $compile, tribe, players, []);
        const rotationNumberElement = statisticsDirective.find('.rotation-number');
        expect(rotationNumberElement.text()).toBe('3');
    }));

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

        it('ordered by longest time since last paired', inject(function ($compile, $rootScope) {
            this.statisticsDirective = buildDirective($rootScope, $compile, this.tribe, this.players, this.history);
            const pairElements = this.statisticsDirective.find('[ng-repeat="report in self.statistics.pairReports"]');
            let numberOfElements = pairElements.length;

            const actualPairedPlayerNames = times((index) => {
                let children = pairElements.eq(index)
                    .find('[ng-repeat="player in report.pair"] text[ng-model="playerCard.player.name"]');
                return [children.eq(0).text(), children.eq(1).text()];
            }, numberOfElements);

            expect(actualPairedPlayerNames).toEqual([
                ['Harry', 'Curly'],
                ['Harry', 'Moe'],
                ['Larry', 'Curly'],
                ['Larry', 'Moe'],
                ['Harry', 'Larry'],
                ['Curly', 'Moe'],
            ]);
        }));

        it('with the time since that pair last occurred', inject(function ($compile, $rootScope) {
            this.statisticsDirective = buildDirective($rootScope, $compile, this.tribe, this.players, this.history);
            const timeElements = this.statisticsDirective.find('[ng-repeat="report in self.statistics.pairReports"] .time-since-last-pairing');

            const timeValues = times(index => timeElements.eq(index).text(), timeElements.length);

            expect(timeValues).toEqual([
                NEVER_PAIRED,
                NEVER_PAIRED,
                NEVER_PAIRED,
                NEVER_PAIRED,
                '0',
                '0'
            ]);
        }));
    });

    it('sends player heat data to subdirective', inject(function ($compile, $rootScope) {

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

        this.statisticsDirective = buildDirective($rootScope, $compile, this.tribe, this.players, this.history);
        const heatmapElement = this.statisticsDirective.find('heatmap');
        const isolateScope = heatmapElement.isolateScope();

        expect(isolateScope.me.data).toEqual([
            [null, 1, 0, 0],
            [1, null, 0, 0],
            [0, 0, null, 1],
            [0, 0, 1, null]
        ]);
    }));

    let getPlayersFromCards = map(function (card) {
        return angular.element(card).isolateScope()["playerCard"].player;
    });

    it('has row of players above heatmap', inject(function ($compile, $rootScope) {
        this.tribe = {id: '2', name: 'Mathematica'};
        this.players = [
            {_id: 'harry', name: 'Harry', tribe: '2'},
            {_id: 'larry', name: 'Larry', tribe: '2'},
            {_id: 'curly', name: 'Curly', tribe: '2'},
            {_id: 'moe', name: 'Moe', tribe: '2'}
        ];

        this.statisticsDirective = buildDirective($rootScope, $compile, this.tribe, this.players, []);
        const playersRowElement = this.statisticsDirective.find(`.${Styles.heatmapPlayersTopRow}`);
        const playerCards = playersRowElement.find('playercard');

        const playersOnCards = getPlayersFromCards(playerCards.toArray());

        expect(playersOnCards).toEqual(this.players);
    }));

    it('has a row of players to the side of the heatmap', inject(function ($compile, $rootScope) {
        this.tribe = {id: '2', name: 'Mathematica'};
        this.players = [
            {_id: 'harry', name: 'Harry', tribe: '2'},
            {_id: 'larry', name: 'Larry', tribe: '2'},
            {_id: 'curly', name: 'Curly', tribe: '2'},
            {_id: 'moe', name: 'Moe', tribe: '2'}
        ];

        this.statisticsDirective = buildDirective($rootScope, $compile, this.tribe, this.players, []);
        const playersRowElement = this.statisticsDirective.find(`.${Styles.heatmapPlayersSideRow}`);
        const playerCards = playersRowElement.find('playercard');

        const playersOnCards = getPlayersFromCards(playerCards.toArray());

        expect(playersOnCards).toEqual(this.players);
    }));

    it('will show the current number of active players', inject(function ($compile, $rootScope) {
        this.tribe = {id: '2', name: 'Mathematica'};
        this.players = [
            {_id: 'harry', name: 'Harry', tribe: '2'},
            {_id: 'larry', name: 'Larry', tribe: '2'},
            {_id: 'curly', name: 'Curly', tribe: '2'},
            {_id: 'moe', name: 'Moe', tribe: '2'}
        ];

        this.statisticsDirective = buildDirective($rootScope, $compile, this.tribe, this.players, []);
        const activePlayerCountElement = this.statisticsDirective.find(`.${Styles.activePlayerCount}`);

        expect(activePlayerCountElement.text()).toEqual('4');
    }));

    it('will show the median spin time', inject(function ($compile, $rootScope) {
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

        this.statisticsDirective = buildDirective($rootScope, $compile, this.tribe, this.players, this.history);
        const medianSpinDurationElement = this.statisticsDirective.find(`.${Styles.medianSpinDuration}`);

        expect(medianSpinDurationElement.text()).toEqual('2 days');
    }));

});