import "ng-fittext";
import "../../../client/app/app";
import Tribe from "../../../common/Tribe";
import Player from "../../../common/Player";

describe('Statistics directive', function () {

    beforeEach(angular.mock.module('coupling'));

    function buildDirective($rootScope, $compile: angular.ICompileService, tribe: Tribe, players: Player[]) {
        const element = angular.element('<statistics tribe="tribe" players="players"/>');
        const scope = $rootScope.$new();
        scope.tribe = tribe;
        scope.players = players;
        const statisticsDirective = $compile(element)(scope);

        scope.$digest();
        return statisticsDirective;
    }

    it('will show a tribe card', inject(function ($compile, $rootScope) {
        const tribe: Tribe = {id: '1', name: 'Super'};
        const statisticsDirective = buildDirective($rootScope, $compile, tribe, []);

        const tribeNameElement = statisticsDirective.find('tribecard .tribe-name');

        expect(tribeNameElement.text()).toBe(tribe.name);
    }));

    it('will show the rotation number', inject(function($compile, $rootScope) {
        const tribe: Tribe = {id: '2', name: 'Mathematica'};
        const players: Player[] = [
            {_id: 'harry', tribe: '2'},
            {_id: 'larry', tribe: '2'},
            {_id: 'curly', tribe: '2'},
            {_id: 'moe', tribe: '2'}
        ];

        const statisticsDirective = buildDirective($rootScope, $compile, tribe, players);
        const rotationNumberElement = statisticsDirective.find('.rotation-number');
        expect(rotationNumberElement.text()).toBe('3');
    }));

});