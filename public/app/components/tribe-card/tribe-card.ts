/// <reference path="../../../../typescript-libraries/typings/tsd.d.ts" />

angular.module('coupling.controllers')
    .controller('TribeCardController', ['$scope', '$location', function ($scope, $location) {
        $scope.clickOnTribeCard = function (tribe) {
            $location.path("/" + tribe._id + "/pairAssignments/current");
        };
        $scope.clickOnTribeName = function (tribe, $event) {
            if ($event.stopPropagation) $event.stopPropagation();
            $location.path("/" + tribe._id + '/edit/');
        };
    }]);

angular.module("coupling.directives")
    .directive('tribecard', function () {
        return {
            controller: 'TribeCardController',
            scope: {
                tribe: '='
            },
            restrict: 'E',
            templateUrl: '/app/components/tribe-card/tribe-card.html'
        }
    });