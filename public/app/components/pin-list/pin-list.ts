import '../controllers'

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