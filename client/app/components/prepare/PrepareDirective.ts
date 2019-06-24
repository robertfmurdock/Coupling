import {module} from "angular";
import Player from "../../../../common/Player";
import Tribe from "../../../../common/Tribe";
import {connectReactToNg} from "../ReactNgAdapter";
import ReactPrepareSpin from "./ReactPrepareSpin";
import {PairAssignmentSet} from "../../../../common";
import PrepareForSpinPage from "./PrepareForSpinPage";

class PrepareController {
    static $inject = ['$location', '$element', '$scope', 'Coupling'];

    players: Player[];
    tribe: Tribe;
    history: PairAssignmentSet[];

    constructor(private $location: angular.ILocationService, $element, $scope, coupling) {
        connectReactToNg({
            component: PrepareForSpinPage,
            props: () => ({
                tribeId: this.tribe.id,
                coupling: coupling
            }),
            domNode: $element[0],
            $scope: $scope,
            watchExpression: "tribeId",
            $location: $location
        });

    }

}

export default module("coupling.prepare", [])
    .controller('PrepareController', PrepareController)
    .directive('prepare', () => {
        return {
            controller: 'PrepareController',
            controllerAs: 'prepare',
            bindToController: true,
            scope: {
                tribe: '=',
                players: '=',
                history: '='
            },
            restrict: 'E',
            template: '<div/>'
        }
    });