import {module} from "angular";
import * as template from "./playercard.pug";
import * as services from "../../services";
import * as styles from './styles.css'

import IController = angular.IController;

export class PlayerCardController implements IController {
    static $inject = ['$location'];

    player: services.Player;
    size: number;
    maxFontHeight: number;
    minFontHeight: number;
    cardStyle;
    styles: any;

    constructor(public $location) {
        this.styles = styles;
    }

    $onInit?() {
        if (!this.size) {
            this.size = 100;
        }

        const pixelWidth = this.size;
        const pixelHeight = (this.size * 1.4);
        const paddingAmount = (this.size * 0.06);
        const borderAmount = (this.size * 0.03);
        this.maxFontHeight = (this.size * 0.3);
        this.minFontHeight = (this.size * 0.175);
        this.cardStyle = {
            width: `${pixelWidth}px`,
            height: `${pixelHeight}px`,
            padding: `${paddingAmount}px`,
            'border-width': `${borderAmount}px`,
        }
    }

    clickPlayerName($event) {
        if ($event.stopPropagation) $event.stopPropagation();
        this.$location.path("/" + this.player.tribe + "/player/" + this.player._id);
    }

}

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