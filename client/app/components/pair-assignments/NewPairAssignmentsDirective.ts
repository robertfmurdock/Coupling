import {module} from "angular";
import {Coupling} from "../../services";
import {connectReactToNg} from "../ReactNgAdapter";
import NewPairAssignmentsPage from "./NewPairAssignmentsPage";

export class PairAssignmentsController {
    static $inject = ['Coupling', '$location', '$scope', '$element'];
    tribeId: string;
    playerIds: string[];

    constructor(public coupling: Coupling, private $location, public $scope, $element?) {
        connectReactToNg({
            component: NewPairAssignmentsPage,
            props: () => ({
                tribeId: this.tribeId,
                playerIds: this.playerIds,
                coupling: coupling
            }),
            domNode: $element[0],
            $scope: $scope,
            $location: $location
        });
    }

}

export default module('coupling.newPairAssignments', [])
    .controller('NewPairAssignmentsController', PairAssignmentsController)
    .directive('newPairAssignments', () => {
        return {
            controller: 'NewPairAssignmentsController',
            controllerAs: 'pairAssignments',
            bindToController: {
                tribeId: '=',
                playerIds: '=',
            },
            restrict: 'E',
            template: '<div/>'
        }
    });
