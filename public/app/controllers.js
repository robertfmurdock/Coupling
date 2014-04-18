"use strict";
var controllers = angular.module('coupling.controllers', []);
controllers.controller('SpinController', ['$scope', '$http', function (scope, http) {
    scope.pairs = [];
    http({method: 'GET', url: '/api/game'}).success(function (data) {
        scope.pairs = data;
    }).error(function (error) {
        console.log(error)
    });
    scope.message = 'Hello From Controller';
}]);