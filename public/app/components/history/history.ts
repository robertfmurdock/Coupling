

angular.module("coupling.directives")
    .directive('history', () => {
        return {
            scope: {
                tribe: '=',
                history: '='
            },
            restrict: 'E',
            templateUrl: '/app/components/history/history.html'
        }
    });