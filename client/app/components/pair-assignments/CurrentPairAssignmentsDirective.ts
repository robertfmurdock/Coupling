import {module} from "angular";
import {Coupling} from "../../services";
import {connectReactToNg} from "../ReactNgAdapter";
import CurrentPairAssignmentsPage from "./CurrentPairAssignmentsPage";

export class PairAssignmentsController {
    static $inject = ['Coupling', '$location', '$scope', '$element'];
    tribeId: string;

    constructor(public coupling: Coupling, private $location, public $scope, $element?) {
        connectReactToNg({
            component: CurrentPairAssignmentsPage,
            props: () => ({
                tribeId: this.tribeId,
                coupling: coupling
            }),
            domNode: $element[0],
            $scope: $scope,
            $location: $location
        });
    }
}

export default module('coupling.currentPairAssignments', [])
    .controller('CurrentPairAssignmentsController', PairAssignmentsController)
    .directive('currentPairAssignments', () => {
        return {
            controller: 'CurrentPairAssignmentsController',
            controllerAs: 'pairAssignments',
            bindToController: {
                tribeId: '=',
            },
            restrict: 'E',
            template: '<div/>'
        }
    });
