import ILocationService = angular.ILocationService;
import Tribe from "../../../common/Tribe";

describe('Pair assignments directive', function () {

    beforeEach(angular.mock.module('coupling'));

    function buildDirective($compile: angular.ICompileService, $rootScope, tribeId) {
        const tribe: Tribe = {id: tribeId, name: "name"};
        const players = [];
        const pairAssignments = [];
        const isNew = false;

        const element = angular.element('<pair-assignments tribe="tribe" players="players" pairAssignments="pairAssignments" isNew="isNew"/>');

        const scope = $rootScope.$new();
        scope.tribe = tribe;
        scope.players = players;
        scope.pairAssignments = pairAssignments;
        scope.isNew = isNew;

        const directive = $compile(element)(scope);

        scope.$digest();
        return directive;
    }

    it('passes down tribe id to the server message', inject(function ($compile, $rootScope) {
        const tribeId = 'whatever';
        const directive = buildDirective($compile, $rootScope, tribeId);
        const serverMessage = directive.find('server-message');
        const isolateScope: any = serverMessage.isolateScope();

        expect(isolateScope.socket.tribeId).toEqual(tribeId);
    }));
});