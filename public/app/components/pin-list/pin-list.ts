/// <reference path="../../../../typescript-libraries/typings/tsd.d.ts" />

angular.module("coupling.directives")
    .directive('pinList', () => {
        return {
            scope: {
                pins: '='
            },
            restrict: 'E',
            templateUrl: '/app/components/pin-list/pin-list.html'
        }
    });