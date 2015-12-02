/// <reference path="../../../../typescript-libraries/typings/tsd.d.ts" />

angular.module("coupling.directives")
    .directive('pairAssignments', () => {
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
        }
    });