import "ng-fittext";
import "../../../client/app/app";
import Tribe from "../../../common/Tribe";

describe('Tribe card directive', function () {

    beforeEach(angular.mock.module('coupling'));
    
    function buildDirective($rootScope, $compile: angular.ICompileService) {
        const element = angular.element('<tribecard tribe="{tribe}"/>');
        const scope = $rootScope.$new();
        const tribeCardDirective = $compile(element)(scope);
        scope.$digest();
        return tribeCardDirective;
    }

    it('will show Unknown when the tribe has no name', inject(function ($compile: angular.ICompileService, $rootScope) {
        const tribe: Tribe = {id: '1', name: ''};
        const tribeCardDirective = buildDirective($rootScope, $compile);

        const nameElement = tribeCardDirective.find('[ng-model="tribecard.tribe.name"]');
        expect(nameElement.text()).toBe('Unknown');
    }));


});