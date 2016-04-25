export default angular.module("coupling.tribeList", [])
    .directive('tribelist', function () {
        return {
            scope: {
                tribes: '='
            },
            restrict: 'E',
            templateUrl: '/app/components/tribe-list/tribe-list.html'
        }
    });