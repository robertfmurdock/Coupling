"use strict";
var directives = angular.module("coupling.directives", []);

directives.directive('tribecard', function () {
  return {
    restrict: 'E',
    templateUrl: '/partials/tribecard/'
  }
});

directives.directive('enterPress', function () {
  return function (scope, element, attrs) {

    element.bind("keydown keypress", function (event) {
      var keyCode = event.which || event.keyCode;

      var enterKeyCode = 13;
      if (keyCode === enterKeyCode) {
        scope.$apply(function () {
          scope.$eval(attrs.enterPress);
        });

        event.preventDefault();
      }
    });
  };
});

