angular.module("coupling.directives")
  .directive('history', function () {
    return {
      scope: {
        tribe: '=',
        history: '='
      },
      restrict: 'E',
      templateUrl: '/app/components/history/history.html'
    }
  });