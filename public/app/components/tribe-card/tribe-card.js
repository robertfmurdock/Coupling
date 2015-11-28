angular.module("coupling.directives")
  .directive('tribecard', function () {
    return {
      restrict: 'E',
      templateUrl: '/app/components/tribe-card/tribe-card.html'
    }
  });