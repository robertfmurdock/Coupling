angular.module('coupling.controllers')
  .controller('SelectedPlayerCardController',
  ['$scope', '$location', 'Coupling', function ($scope, $location) {
    $scope.clickPlayerCard = function () {
      $scope.player.isAvailable = !$scope.player.isAvailable;
    };
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
      templateUrl: '/partials/playercard/',
      bindToController: true
    }
  });
