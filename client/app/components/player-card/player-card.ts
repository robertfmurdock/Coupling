import {module} from "angular";
import * as template from "./playercard.pug";
import * as styles from './styles.css'

import IController = angular.IController;
import Player from "../../../../common/Player";

export class PlayerCardController implements IController {
    static $inject = ['$location'];

    styles: any;
    player: Player;
    size: number;
    disabled: boolean;
    maxFontHeight: number;
    minFontHeight: number;
    cardStyle: any;
    headerStyle: any;

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

        const borderAmount = (this.size * 0.01);
        this.maxFontHeight = (this.size * 0.31);
        this.minFontHeight = (this.size * 0.16);
        this.cardStyle = {
            width: `${pixelWidth}px`,
            height: `${pixelHeight}px`,
            padding: `${paddingAmount}px`,
            'border-width': `${borderAmount}px`,
        };
        const headerMargin = (this.size * 0.02);
        this.headerStyle = { margin: `${headerMargin}px 0 0 0`, }
    }

    clickPlayerName($event) {
        if(this.disabled) {
            return;
        }
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
                size: '=?',
                disabled: '=?'
            },
            bindToController: true
        }
    });