import {module} from "angular";
import Tribe from "../../../../common/Tribe";
import {connectReactToNg} from "../ReactNgAdapter";
import ReactRetiredPlayers from "./ReactRetiredPlayers";
import Player from "../../../../common/Player";
import RetiredPlayersPage from "./RetiredPlayersPage";

export class RetiredPlayersController {
    static $inject = ['$location', '$element', '$scope', 'Coupling'];
    tribeId: string;

    constructor($location, $element, $scope, coupling) {
        connectReactToNg({
            component: RetiredPlayersPage,
            props: () => ({tribeId: this.tribeId, coupling}),
            domNode: $element[0],
            $scope: $scope,
            watchExpression: "tribeId",
            $location: $location
        });
    }
}

export default module("coupling.retiredPlayers", [])
    .directive('retiredPlayers', () => {
        return {
            controller: RetiredPlayersController,
            scope: {tribeId: '='},
            bindToController: true,
            restrict: 'E',
            template: '<div />'
        }
    });