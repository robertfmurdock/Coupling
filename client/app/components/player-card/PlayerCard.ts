import * as services from '../../services'

export default class PlayerCardController {
    static $inject = ['$location'];

    player:services.Player;
    size:number;

    constructor(public $location) {
        if (!this.size) {
            this.size = 100;
        }
    }

    clickPlayerName($event) {
        if ($event.stopPropagation) $event.stopPropagation();
        this.$location.path("/" + this.player.tribe + "/player/" + this.player._id);
    }

}