import * as template from './statistics.pug';

export class StatisticsController {
}

export default angular.module('coupling.statistics', [])
    .directive('statistics', function () {
        return {
            controllerAs: 'self',
            controller: StatisticsController,
            bindToController: true,
            scope: {
                tribe: '=tribe'
            },
            template: template
        }
    });