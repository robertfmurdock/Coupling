import * as template from './pin-list.pug'

export default angular.module("coupling.pinList", [])
    .directive('pinList', () => {
        return {
            scope: {
                pins: '='
            },
            restrict: 'E',
            template: template
        }
    });