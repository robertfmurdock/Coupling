/// <reference path="../../../../typescript-libraries/typings/tsd.d.ts" />
angular.module("coupling.directives")
    .directive('history', function () {
    return {
        scope: {
            tribe: '=',
            history: '='
        },
        restrict: 'E',
        templateUrl: '/app/components/history/history.html'
    };
});
//# sourceMappingURL=history.js.map