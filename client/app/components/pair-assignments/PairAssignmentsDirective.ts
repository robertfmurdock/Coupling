import {module} from "angular";
import Tribe from "../../../../common/Tribe";
import PairAssignmentSet from "../../../../common/PairAssignmentSet";
import Player from "../../../../common/Player";
import {Coupling} from "../../services";
import {connectReactToNg} from "../ReactNgAdapter";
import ReactPairAssignments from "./ReactPairAssignments";

export class PairAssignmentsController {
    static $inject = ['Coupling', '$location', '$scope', '$element'];
    tribe: Tribe;
    players: Player[];
    pairAssignments: PairAssignmentSet;
    isNew: boolean;

    constructor(public coupling: Coupling, private $location, public $scope, $element?) {
        connectReactToNg({
            component: ReactPairAssignments,
            props: () => ({
                tribe: this.tribe,
                pairAssignments: this.pairAssignments,
                isNew: this.isNew,
                players: this.players,
                coupling: coupling
            }),
            domNode: $element[0],
            $scope: $scope,
            watchExpression: "pairAssignments",
            $location: $location
        });
    }

}

export default module('coupling.pairAssignments', [])
    .controller('PairAssignmentsController', PairAssignmentsController)
    .directive('pairAssignments', () => {
        return {
            controller: 'PairAssignmentsController',
            controllerAs: 'pairAssignments',
            bindToController: {
                tribe: '=',
                players: '=',
                pairAssignments: '=pairs',
                isNew: '='
            },
            restrict: 'E',
            template: '<div/>'
        }
    });
