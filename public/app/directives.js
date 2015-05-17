"use strict";
var directives = angular.module("coupling.directives", []);

directives.directive('tribecard', function(){
  return {
    restrict: 'E',
    templateUrl: '/partials/tribecard/'
  }
});