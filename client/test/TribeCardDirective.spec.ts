import * as angular from 'angular'
import "ng-fittext";
import "../app/app";
import Tribe from "../../common/Tribe";

describe('Tribe card directive', function () {

    beforeEach(angular.mock.module('coupling'));

    function buildDirective($rootScope, $compile: angular.ICompileService, tribe: Tribe) {
        const element = angular.element('<tribecard tribe="tribe"/>');
        const scope = $rootScope.$new();
        scope.tribe = tribe;
        const tribeCardDirective = $compile(element)(scope);
        scope.$digest();
        return tribeCardDirective;
    }

    it('will show Unknown when the tribe has no name', inject(function ($compile, $rootScope) {
        const tribe: Tribe = {id: '1', name: ''};
        const tribeCardDirective = buildDirective($rootScope, $compile, tribe);

        const nameElement = tribeCardDirective.find('.tribe-card-header');
        expect(nameElement.text()).toBe('Unknown');
    }));

});