"use strict";

angular.module("coupling.directives")
  .directive('tribelist', function () {
    return {
      restrict: 'E',
      templateUrl: '/app/components/tribe-list/tribe-list.html'
    }
  });