/// <reference path="../../../../typescript-libraries/typings/tsd.d.ts" />

angular.module("coupling.controllers")
    .controller('PrepareController', ['$scope', '$location', 'Coupling',
        ($scope, $location, Coupling) => {

            $scope.clickPlayerCard = player => {
                player.isAvailable = !player.isAvailable;
            };

            $scope.clickSpinButton = () => {
                Coupling.data.players = $scope.players;
                $location.path($scope.tribe._id + "/pairAssignments/new");
            };
        }]);


angular.module("coupling.directives")
    .directive('prepare', () => {
        return {
            controller: 'PrepareController',
            scope: {
                tribe: '=',
                players: '='
            },
            restrict: 'E',
            templateUrl: '/app/components/prepare/prepare.html'
        }
    });