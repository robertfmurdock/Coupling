"use strict";
angular.module('coupling.controllers')
  .controller('SelectedPlayerCardController',
  ['$scope', '$location', 'Coupling', function ($scope, $location) {
    if (!$scope.size) {
      $scope.size = 100;
    }
    $scope.clickPlayerName = function ($event) {
      if ($event.stopPropagation) $event.stopPropagation();
      $location.path("/" + $scope.player.tribe + "/player/" + $scope.player._id);
    };
  }]);

angular.module("coupling.directives")
  .directive('selectedplayercard', function () {
    return {
      restrict: 'E',
      controller: 'SelectedPlayerCardController',
      templateUrl: '/app/components/player-card/playercard.html',
      scope: {
        player: '=',
        size: '=?'
      }
    }
  })
  .directive('playercard', function () {
    return {
      restrict: 'E',
      controller: ['$scope', function ($scope) {
        console.log($scope);
        if (!$scope.size) {
          $scope.size = 100;
        }
      }],
      scope: {
        player: '=',
        size: '=?'
      },
      templateUrl: '/app/components/player-card/playercard.html'
    }
  });
