"use strict";
var app = angular.module('coupling', ["ngRoute", 'ui.gravatar', 'coupling.controllers']);

app.config(['$locationProvider', function ($locationProvider) {
    $locationProvider.html5Mode(true);
}]);
app.config(['$routeProvider', function (routeProvider) {
    routeProvider.when('/pairAssignments/:pairAssignmentsId/', {templateUrl: '/partials/pairAssignments/', controller: "PairAssignmentsController"});
}]);
console.info(angular.module('ui.gravatar'));
angular.module('ui.gravatar').config([
    'gravatarServiceProvider', function (gravatarServiceProvider) {
        gravatarServiceProvider.defaults = {
            size: 100,
            "default": 'mm'  // Mystery man as default for missing avatars
        };

        gravatarServiceProvider.secure = true;
    }
]);