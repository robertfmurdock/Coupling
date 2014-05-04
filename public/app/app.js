"use strict";
var app = angular.module('coupling', ["ngRoute", 'ui.gravatar', 'ngDragDrop', 'coupling.controllers', 'coupling.filters', 'coupling.animations']);

app.config(['$locationProvider', function ($locationProvider) {
    $locationProvider.html5Mode(true);
}]);
app.config(['$routeProvider', function (routeProvider) {
    routeProvider.when('/tribes/', {templateUrl: '/partials/tribe-list/', controller: "TribeListController"});
    routeProvider.when('/new-tribe/', {templateUrl: '/partials/tribe/', controller: "NewTribeController"});
    routeProvider.when('/:tribeId/history', {templateUrl: '/partials/history/', controller: "HistoryController"});
    routeProvider.when('/:tribeId/pairAssignments/current/', {templateUrl: '/partials/pairAssignments/', controller: "CurrentPairAssignmentsController"});
    routeProvider.when('/:tribeId/pairAssignments/new/', {templateUrl: '/partials/pairAssignments/', controller: "NewPairAssignmentsController"});
    routeProvider.when('/:tribeId/player/new/', {templateUrl: '/partials/player/', controller: "NewPlayerController"});
    routeProvider.when('/:tribeId/player/:id/', {templateUrl: '/partials/player/', controller: "EditPlayerController"});
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