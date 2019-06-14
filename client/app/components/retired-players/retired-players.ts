import {module} from "angular";
import Tribe from "../../../../common/Tribe";
import {connectReactToNg} from "../ReactNgAdapter";
import ReactRetiredPlayers from "./ReactRetiredPlayers";
import Player from "../../../../common/Player";

export class RetiredPlayersController {
    static $inject = ['$location', '$element', '$scope'];
    tribe: Tribe;
    retiredPlayers: Player[];

    constructor($location, $element, $scope) {
        connectReactToNg({
            component: ReactRetiredPlayers,
            props: () => ({
                tribe: this.tribe,
                retiredPlayers: this.retiredPlayers
            }),
            domNode: $element[0],
            $scope: $scope,
            watchExpression: "player",
            $location: $location
        });
    }

}

export default module("coupling.retiredPlayers", [])
    .directive('retiredPlayers', () => {
        return {
            controller: RetiredPlayersController,
            scope: {
                retiredPlayers: '=',
                tribe: '='
            },
            bindToController: true,
            restrict: 'E',
            template: '<div />'
        }
    });