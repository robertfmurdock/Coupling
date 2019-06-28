import {module} from "angular";
import Tribe from "../../../../common/Tribe";
import PairAssignmentSet from "../../../../common/PairAssignmentSet";
import {Coupling} from "../../services";
import ReactHistory from "./ReactHistory";
import {connectReactToNg} from "../ReactNgAdapter";
import HistoryPage from "./HistoryPage";

export class HistoryController {

    static $inject = ['Coupling', '$route', '$scope', '$element', '$location'];

    tribeId: string;
    coupling: Coupling;
    private route: any;

    constructor(coupling, route, $scope, $element, $location) {
        this.coupling = coupling;
        this.route = route;

        connectReactToNg({
            component: HistoryPage,
            props: () => ({
                tribeId: this.tribeId,
                coupling: this.coupling,
                reload: () => $scope.$apply(() => route.reload())
            }),
            domNode: $element[0],
            $scope: $scope,
            watchExpression: "tribeId",
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
                tribeId: '='
            },
            restrict: 'E',
            template: "<div />"
        }
    });