"use strict";
angular.module("coupling.directives").directive('pinList', function () {
    return {
        scope: {
            pins: '='
        },
        restrict: 'E',
        templateUrl: '/app/components/pin-list/pin-list.html'
    };
});
//# sourceMappingURL=pin-list.js.map