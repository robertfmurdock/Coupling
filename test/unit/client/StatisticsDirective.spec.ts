import "ng-fittext";
import "../../../client/app/app";
import Tribe from "../../../common/Tribe";

describe('Statistics directive', function () {

    beforeEach(angular.mock.module('coupling'));

    function buildDirective($rootScope, $compile: angular.ICompileService, tribe: Tribe) {
        const element = angular.element('<statistics tribe="tribe"/>');
        const scope = $rootScope.$new();
        scope.tribe = tribe;
        const statisticsDirective = $compile(element)(scope);

        scope.$digest();
        return statisticsDirective;
    }

    it('will show a tribe card', inject(function ($compile, $rootScope) {
        const tribe: Tribe = {id: '1', name: 'Super'};
        const statisticsDirective = buildDirective($rootScope, $compile, tribe);

        const tribeNameElement = statisticsDirective.find('tribecard .tribe-name');
        expect(tribeNameElement.text()).toBe(tribe.name);
    }));

});