import {module} from "angular";
import * as template from './history.pug'
import Tribe from "../../../../common/Tribe";

export class HistoryController {

    tribe: Tribe;

    removeEntry(entry) {
        if (confirm("Are you sure you want to delete these pair assignments?")) {
            entry.$remove();
        }
    }

}

export default module("coupling.history", [])
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