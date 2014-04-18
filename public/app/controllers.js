"use strict";
var controllers = angular.module('coupling.controllers', []);

controllers.controller('CouplingController', ['$scope', '$http', '$location', function (scope, http, location) {
    scope.spin = function () {
        http({method: 'GET', url: '/api/game'}).success(function (data) {
            scope.pairs = data;
        }).error(function (error) {
            console.log(error)
        });
        location.path("/pairAssignments");
    }
}]);

controllers.controller('PairAssignmentsController', ['$scope', function (scope) {

}]);

