export default angular.module("coupling.pinList", [])
    .directive('pinList', () => {
        return {
            scope: {
                pins: '='
            },
            restrict: 'E',
            templateUrl: '/app/components/pin-list/pin-list.html'
        }
    });