/// <reference path="../../../../typescript-libraries/typings/tsd.d.ts" />
angular.module("coupling.directives")
    .directive('playerRoster', function () {
    return {
        scope: {
            tribe: '=',
            players: '=',
            label: '=?'
        },
        restrict: 'E',
        templateUrl: '/app/components/player-roster/player-roster.html'
    };
});
//# sourceMappingURL=player-roster.js.map