import {module} from "angular";
import PlayerCardController from "./PlayerCard";
import * as template from "./playercard.pug";

export default module('coupling.playerCard', [])
    .controller('PlayerCardController', PlayerCardController)
    .directive('playercard', () => {
        return {
            template: template,
            restrict: 'E',
            controller: 'PlayerCardController',
            controllerAs: 'playerCard',
            scope: {
                player: '=',
                size: '=?'
            },
            bindToController: true
        }
    });