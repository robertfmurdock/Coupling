"use strict";

angular.module("coupling.controllers")
  .controller('TribeConfigController', ['$scope', 'Coupling', '$location',
    function ($scope, Coupling, $location) {
      $scope.clickSaveButton = function () {
        Coupling.saveTribe($scope.tribe)
          .then(function () {
            $location.path("/tribes");
          });
      }
    }]);

angular.module("coupling.directives")
  .directive('tribeConfig', function () {
    return {
      controller: 'TribeConfigController',
      bindToController: true,
      restrict: 'E',
      templateUrl: '/app/components/tribe-config/tribe-config.html'
    }
  });