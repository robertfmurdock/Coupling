import {module} from "angular";
import Player from "../../../../common/Player";
import Tribe from "../../../../common/Tribe";
import {connectReactToNg} from "../ReactNgAdapter";
import ReactPrepareSpin from "./ReactPrepareSpin";
import {PairAssignmentSet} from "../../../../common";

class PrepareController {
    static $inject = ['$location', '$element', '$scope'];

    players: Player[];
    tribe: Tribe;
    history: PairAssignmentSet[];

    constructor(private $location: angular.ILocationService, $element, $scope) {
        connectReactToNg({
            component: ReactPrepareSpin,
            props: () => ({
                players: this.players,
                tribe: this.tribe,
                history: this.history
            }),
            domNode: $element[0],
            $scope: $scope,
            watchExpression: "players",
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