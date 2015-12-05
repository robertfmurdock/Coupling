/// <reference path="../../../../typescript-libraries/typings/tsd.d.ts" />
/// <reference path="../../services.ts" />
var PlayerCardController = (function () {
    function PlayerCardController($location) {
        this.$location = $location;
        if (!this.size) {
            this.size = 100;
        }
    }
    PlayerCardController.prototype.clickPlayerName = function ($event) {
        if ($event.stopPropagation)
            $event.stopPropagation();
        this.$location.path("/" + this.player.tribe + "/player/" + this.player._id);
    };
    PlayerCardController.$inject = ['$location'];
    return PlayerCardController;
})();
angular.module('coupling.controllers').controller('PlayerCardController', PlayerCardController);
angular.module("coupling.directives").directive('playercard', function () {
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
    };
});
//# sourceMappingURL=player-card.js.map