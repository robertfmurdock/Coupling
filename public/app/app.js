"use strict";
var app = angular.module('coupling', ["ngRoute", 'ui.gravatar', 'ang-drag-drop', 'coupling.controllers', 'coupling.filters', 'coupling.animations']);

app.config(['$locationProvider', function ($locationProvider) {
  $locationProvider.html5Mode({
    enabled: true,
    requireBase: false
  });
}]);
app.config(['$routeProvider', function (routeProvider) {
  routeProvider.when('/tribes/', {
    templateUrl: '/partials/tribe-list/',
    controller: "TribeListController",
    resolve: {
      tribes: ['Coupling', function (Coupling) {
        return Coupling.getTribes();
      }]
    }
  });
  routeProvider.when('/new-tribe/', {
    templateUrl: '/partials/tribe/',
    controller: "NewTribeController"
  });
  routeProvider.when('/:tribeId/', {
    templateUrl: '/partials/tribe/',
    controller: "EditTribeController",
    resolve: {
      tribe: ['$route', 'Coupling', function ($route, Coupling) {
        return Coupling.requestSpecificTribe($route.current.params.tribeId);
      }]
    }
  });
  routeProvider.when('/:tribeId/history', {
    templateUrl: '/partials/history/',
    controller: "HistoryController"
  });
  routeProvider.when('/:tribeId/pins', {
    templateUrl: '/partials/pin-list/',
    controller: 'PinListController'
  });
  routeProvider.when('/:tribeId/pairAssignments/current/', {
    templateUrl: '/partials/pairAssignments/',
    controller: "CurrentPairAssignmentsController",
    resolve: {
      history: ['$route', 'Coupling', function ($route, Coupling) {
        return Coupling.selectTribe($route.current.params.tribeId).then(function (data) {
          return data.history;
        });
      }]
    }
  });
  routeProvider.when('/:tribeId/pairAssignments/new/', {
    templateUrl: '/partials/pairAssignments/',
    controller: "NewPairAssignmentsController"
  });
  routeProvider.when('/:tribeId/player/new/', {
    templateUrl: '/partials/player/',
    controller: "NewPlayerController"
  });
  routeProvider.when('/:tribeId/player/:id/', {
    templateUrl: '/partials/player/',
    controller: "EditPlayerController"
  });
  routeProvider.when('/auth/google', {
    redirectTo: '/auth/google'
  });
  routeProvider.when('/v2/tribes', {
    templateUrl: '/view-revamp/partials/tribe-list',
    controller: 'TribeListControllerRevamp',
    resolve: {
      tribes: ['Coupling', function (Coupling) {
        return Coupling.getTribes();
      }]
    }
  });
}]);

angular.module('ui.gravatar').config([
  'gravatarServiceProvider',
  function (gravatarServiceProvider) {
    gravatarServiceProvider.defaults = {
      size: 100,
      "default": 'mm'
    };
    gravatarServiceProvider.secure = true;
  }
]);