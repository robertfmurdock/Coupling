import * as template from './tribe-list.pug'

export default angular.module("coupling.tribeList", [])
    .directive('tribelist', function () {
        return {
            scope: {
                tribes: '='
            },
            restrict: 'E',
            template: template
        }
    });