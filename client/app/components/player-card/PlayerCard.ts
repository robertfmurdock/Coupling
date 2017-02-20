import * as services from "../../services";
import IController = angular.IController;

export default class PlayerCardController implements IController {
    static $inject = ['$location'];

    player: services.Player;
    size: number;
    cardStyle;

    constructor(public $location) {
    }

    $onInit?() {
        if (!this.size) {
            this.size = 100;
        }
        const pixelWidth = this.size;
        const pixelHeight = (this.size * 1.4);
        this.cardStyle = {width: pixelWidth + "px", height: pixelHeight + "px"}
    }

    clickPlayerName($event) {
        if ($event.stopPropagation) $event.stopPropagation();
        this.$location.path("/" + this.player.tribe + "/player/" + this.player._id);
    }

}