import {module} from "angular";
import * as ReactDOM from 'react-dom'
import * as React from 'react'

import IController = angular.IController;
import Player from "../../../../common/Player";
import ReactPlayerCard from "./ReactPlayerCard";

export class PlayerCardController implements IController {
    static $inject = ['$location', '$scope', '$element'];

    player: Player;
    tribeId: string;
    size: number;
    disabled: boolean;

    constructor(public $location, $scope, element) {

        const renderReactElement = () => {
            if (this.player) {
                ReactDOM.render(
                    React.createElement(ReactPlayerCard, {
                        player: this.player,
                        size: this.size,
                        tribeId: this.tribeId,
                        disabled: this.disabled
                    }),
                    element[0]
                );
            }
        };

        $scope.$watch("player", renderReactElement);
        $scope.$on("$destroy", unmountReactElement);

        function unmountReactElement() {
            ReactDOM.unmountComponentAtNode(element[0]);
        }
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