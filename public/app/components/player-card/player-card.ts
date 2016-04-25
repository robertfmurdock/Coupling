import * as services from '../../services'
import '../controllers'

class PlayerCardController {
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

angular.module('coupling.controllers')
    .controller('PlayerCardController', PlayerCardController);

angular.module("coupling.directives")
    .directive('playercard', () => {
        return {
            templateUrl: '/app/components/player-card/playercard.html',
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