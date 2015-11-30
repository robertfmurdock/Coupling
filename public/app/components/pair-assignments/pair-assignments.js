angular.module("coupling.directives")
  .directive('pairAssignments', function () {
    return {
      scope: {
        tribe: '=',
        players: '=',
        currentPairAssignments: '=pairs',
        unpairedPlayers: '=',
        save: '=',
        onDrop: '='
      },
      restrict: 'E',
      templateUrl: '/app/components/pair-assignments/pair-assignments.html'
    }
  });