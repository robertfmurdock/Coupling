import {module} from "angular";
import * as template from './history.pug'
import Tribe from "../../../../common/Tribe";
import PairAssignmentSet from "../../../../common/PairAssignmentSet";
import {Coupling} from "../../services";

export class HistoryController {

    static $inject = ['Coupling', '$route'];

    tribe: Tribe;
    coupling: Coupling;
    private route: any;

    constructor(coupling, route) {
        this.coupling = coupling;
        this.route = route;
    }

    async removeEntry(entry : PairAssignmentSet) {
        if (confirm("Are you sure you want to delete these pair assignments?")) {
            await this.coupling.removeAssignments(entry)
            this.route.reload()
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