import '../controllers'

angular.module("coupling.directives")
    .directive('tribelist', function () {
        return {
            scope: {
                tribes: '='
            },
            restrict: 'E',
            templateUrl: '/app/components/tribe-list/tribe-list.html'
        }
    });