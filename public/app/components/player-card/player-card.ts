/// <reference path="../../../../typescript-libraries/typings/tsd.d.ts" />

angular.module('coupling.controllers')
    .controller('PlayerCardController',
    ['$scope', '$location', ($scope, $location) => {
        if (!$scope.size) {
            $scope.size = 100;
        }
        $scope.clickPlayerName = function ($event) {
            if ($event.stopPropagation) $event.stopPropagation();
            $location.path("/" + $scope.player.tribe + "/player/" + $scope.player._id);
        };
    }]);

angular.module("coupling.directives")
    .directive('playercard', () => {
        return {
            restrict: 'E',
            controller: 'PlayerCardController',
            templateUrl: '/app/components/player-card/playercard.html',
            scope: {
                player: '=',
                size: '=?'
            }
        }
    });