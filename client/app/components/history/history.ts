import {module} from "angular";
import Tribe from "../../../../common/Tribe";
import PairAssignmentSet from "../../../../common/PairAssignmentSet";
import {Coupling} from "../../services";
import ReactHistory from "./ReactHistory";
import {connectReactToNg} from "../ReactNgAdapter";

export class HistoryController {

    static $inject = ['Coupling', '$route', '$scope', '$element', '$location'];

    tribe: Tribe;
    history: PairAssignmentSet[];
    coupling: Coupling;
    private route: any;

    constructor(coupling, route, $scope, $element, $location) {
        this.coupling = coupling;
        this.route = route;

        connectReactToNg({
            component: ReactHistory,
            props: () => ({
                tribe: this.tribe,
                history: this.history,
                coupling: this.coupling,
                reload: () => $scope.$apply(() => route.reload())
            }),
            domNode: $element[0],
            $scope: $scope,
            watchExpression: "history",
            $location: $location
        });
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
            template: "<div />"
        }
    });