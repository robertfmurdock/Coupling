import {module} from "angular";
import Player from "../../../../common/Player";
import ReactPlayerCard from "./ReactPlayerCard";
import IController = angular.IController;
import {connectReactToNg} from "../ReactNgAdapter";

export class PlayerCardController implements IController {
    static $inject = ['$location', '$scope', '$element'];

    player: Player;
    tribeId: string;
    size: number;
    disabled: boolean;

    constructor(public $location, $scope, element) {
        connectReactToNg({
            component: ReactPlayerCard,
            props: () => ({
                player: this.player,
                size: this.size,
                tribeId: this.tribeId,
                disabled: this.disabled
            }),
            domNode: element[0],
            $scope: $scope,
            watchExpression: "player"
        });
    }
}

export default module('coupling.playerCard', [])
    .controller('PlayerCardController', PlayerCardController)
    .directive('playercard', () => {
        return {
            template: '<div/>',
            restrict: 'E',
            controller: 'PlayerCardController',
            controllerAs: 'playerCard',
            scope: {
                tribeId: '=',
                player: '=',
                size: '=?',
                disabled: '=?'
            },
            bindToController: true
        }
    });