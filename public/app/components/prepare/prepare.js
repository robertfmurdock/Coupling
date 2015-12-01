angular.module("coupling.controllers")
  .controller('PrepareController', ['$scope', '$location', 'Coupling',
    function ($scope, $location, Coupling) {
      $scope.clickSpinButton = function () {
        Coupling.data.players = $scope.players;
        $location.path($scope.tribe._id + "/pairAssignments/new");
      };
    }]);

angular.module("coupling.directives")
  .directive('prepare', function () {
    return {
      controller: 'PrepareController',
      scope: {
        tribe: '=',
        players: '='
      },
      restrict: 'E',
      templateUrl: '/app/components/prepare/prepare.html'
    }
  });