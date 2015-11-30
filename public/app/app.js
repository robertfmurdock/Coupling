"use strict";
var app = angular.module('coupling', ["ngRoute",
  'ngFitText',
  'ui.gravatar',
  'ang-drag-drop',
  'coupling.controllers',
  'coupling.filters',
  'coupling.directives',
  'coupling.animations']);

app.config(['$locationProvider', function ($locationProvider) {
  $locationProvider.html5Mode({
    enabled: true,
    requireBase: false
  });
}]);

app.config(['$routeProvider', function (routeProvider) {

  var tribeResolution = ['$route', 'Coupling', function ($route, Coupling) {
    return Coupling.requestSpecificTribe($route.current.params.tribeId);
  }];

  routeProvider
    .when('/', {redirectTo: '/tribes/'})
    .when('/tribes/', {
      template: '<tribelist>',
      controller: ['$scope', 'tribes', function ($scope, tribes) {
        $scope.tribes = tribes;
      }],
      resolve: {
        tribes: ['Coupling', function (Coupling) {
          return Coupling.getTribes();
        }]
      }
    })
    .when('/new-tribe/', {
      template: '<tribe-config>',
      controller: ['$scope', 'Coupling', function ($scope, Coupling) {
        $scope.tribe = new Coupling.Tribe();
        $scope.tribe.name = 'New Tribe';
        $scope.isNew = true;
      }]
    })
    .when('/:tribeId/', {
      redirectTo: '/:tribeId/pairAssignments/current/'
    })
    .when('/:tribeId/prepare/', {
      templateUrl: '/partials/prepare/',
      controller: 'PrepareController',
      resolve: {
        tribe: tribeResolution,
        players: ['$route', 'Coupling', function ($route, Coupling) {
          return Coupling.requestPlayersPromise($route.current.params.tribeId,
            Coupling.requestHistoryPromise($route.current.params.tribeId));
        }]
      }
    })
    .when('/:tribeId/edit/', {
      template: '<tribe-config>',
      controller: ['$scope', 'tribe', function ($scope, tribe) {
        $scope.tribe = tribe;
        $scope.isNew = false;
      }],
      resolve: {
        tribe: tribeResolution
      }
    })
    .when('/:tribeId/history/', {
      templateUrl: '/partials/history/',
      controller: "HistoryController",
      resolve: {
        tribe: tribeResolution,
        history: ['$route', 'Coupling', function ($route, Coupling) {
          return Coupling.requestHistoryPromise($route.current.params.tribeId);
        }]
      }
    })
    .when('/:tribeId/pins', {
      templateUrl: '/partials/pin-list/',
      controller: 'PinListController'
    })
    .when('/:tribeId/pairAssignments/current/', {
      templateUrl: '/partials/pairAssignments/',
      controller: "CurrentPairAssignmentsController",
      resolve: {
        pairAssignmentDocument: ['$route', 'Coupling', function ($route, Coupling) {
          return Coupling.requestHistoryPromise($route.current.params.tribeId).then(function (history) {
            return history[0];
          });
        }],
        tribe: tribeResolution,
        players: ['$route', 'Coupling', function ($route, Coupling) {
          return Coupling.requestPlayersPromise($route.current.params.tribeId,
            Coupling.requestHistoryPromise($route.current.params.tribeId));
        }]
      }
    })
    .when('/:tribeId/pairAssignments/new/', {
      templateUrl: '/partials/pairAssignments/',
      controller: "NewPairAssignmentsController",
      resolve: {
        tribe: tribeResolution,
        players: ['$route', 'Coupling', function ($route, Coupling) {
          return Coupling.requestPlayersPromise($route.current.params.tribeId,
            Coupling.requestHistoryPromise($route.current.params.tribeId));
        }]
      }
    })
    .when('/:tribeId/player/new/', {
      templateUrl: '/partials/player/',
      controller: "NewPlayerController",
      resolve: {
        tribe: tribeResolution,
        players: ['$route', 'Coupling', function ($route, Coupling) {
          return Coupling.requestPlayersPromise($route.current.params.tribeId,
            Coupling.requestHistoryPromise($route.current.params.tribeId));
        }]
      }
    })
    .when('/:tribeId/player/:id/', {
      templateUrl: '/partials/player/',
      controller: "EditPlayerController",
      resolve: {
        tribe: tribeResolution,
        players: ['$route', 'Coupling', function ($route, Coupling) {
          return Coupling.requestPlayersPromise($route.current.params.tribeId,
            Coupling.requestHistoryPromise($route.current.params.tribeId));
        }]
      }
    })
    .when('/auth/google', {
      redirectTo: '/auth/google'
    });
}]);

angular.module('ui.gravatar')
  .config([
    'gravatarServiceProvider',
    function (gravatarServiceProvider) {
      gravatarServiceProvider.defaults = {
        size: 100,
        "default": 'mm'
      };
      gravatarServiceProvider.secure = true;
    }
  ]);

angular.module("coupling.directives", []);