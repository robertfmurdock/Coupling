/// <reference path="../../../../typescript-libraries/typings/tsd.d.ts" />
angular.module("coupling.controllers").controller('TribeConfigController', [
    '$scope',
    'Coupling',
    '$location',
    function ($scope, Coupling, $location) {
        $scope.clickSaveButton = function () {
            Coupling.saveTribe($scope.tribe).then(function () {
                $location.path("/tribes");
            });
        };
    }
]);
angular.module("coupling.directives").directive('tribeConfig', function () {
    return {
        controller: 'TribeConfigController',
        scope: {
            tribe: '=tribe',
            isNew: '=isNew'
        },
        restrict: 'E',
        templateUrl: '/app/components/tribe-config/tribe-config.html'
    };
});
//# sourceMappingURL=tribe-config.js.map