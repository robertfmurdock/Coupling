import * as services from '../../services'
import * as template from './history.pug'

export class HistoryController {

    tribe:services.Tribe;

    removeEntry(entry) {
        if (confirm("Are you sure you want to delete these pair assignments?")) {
            entry.$remove();
        }
    }

}

export default angular.module("coupling.history", [])
    .controller('HistoryController', HistoryController)
    .directive('history', () => {
        return {
            controller: HistoryController,
            controllerAs: 'history',
            bindToController: true,
            scope: {
                tribe: '=',
                history: '='
            },
            restrict: 'E',
            template: template
        }
    });