"use strict";
var app = angular.module('coupling', ["ngRoute", 'coupling.controllers']);

app.config(['$locationProvider', function ($locationProvider) {
    $locationProvider.html5Mode(true);
}]);
app.config(['$routeProvider', function ($routeProvider) {
    $routeProvider.when('/spin', {templateUrl: 'partials/spinResults', controller: "SpinController"});
}]);

