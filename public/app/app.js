"use strict";
var app = angular.module('coupling', ["ngRoute", 'ui.gravatar', 'ngDragDrop', 'coupling.controllers', 'coupling.filters', 'coupling.animations']);

app.config(['$locationProvider', function ($locationProvider) {
    $locationProvider.html5Mode(true);
}]);
app.config(['$routeProvider', function (routeProvider) {
    routeProvider.when('/tribes/', {templateUrl: '/partials/tribes/', controller: "TribesController"});
    routeProvider.when('/pairAssignments/current/', {templateUrl: '/partials/pairAssignments/', controller: "CurrentPairAssignmentsController"});
    routeProvider.when('/pairAssignments/new/', {templateUrl: '/partials/pairAssignments/', controller: "NewPairAssignmentsController"});
    routeProvider.when('/player/new/', {templateUrl: '/partials/player/', controller: "NewPlayerController"});
    routeProvider.when('/player/:id/', {templateUrl: '/partials/player/', controller: "EditPlayerController"});
}]);

angular.module('ui.gravatar').config([
    'gravatarServiceProvider', function (gravatarServiceProvider) {
        gravatarServiceProvider.defaults = {
            size: 100,
            "default": 'mm'
        };
        gravatarServiceProvider.secure = true;
    }
]);