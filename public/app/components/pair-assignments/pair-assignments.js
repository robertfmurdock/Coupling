/// <reference path="../../../../typescript-libraries/typings/tsd.d.ts" />
angular.module("coupling.directives").directive('pairAssignments', function () {
    return {
        scope: {
            tribe: '=',
            players: '=',
            currentPairAssignments: '=pairs',
            unpairedPlayers: '=',
            save: '=',
            onDrop: '='
        },
        restrict: 'E',
        templateUrl: '/app/components/pair-assignments/pair-assignments.html'
    };
});
//# sourceMappingURL=pair-assignments.js.map