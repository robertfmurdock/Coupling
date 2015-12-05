/// <reference path="../../../../typescript-libraries/typings/tsd.d.ts" />
/// <reference path="../../services.ts" />

class PrepareController {
    static $inject = ['$location', 'Coupling'];

    players:[Player];
    tribe:Tribe;

    constructor(private $location, private Coupling) {
    }

    clickPlayerCard(player) {
        player.isAvailable = !player.isAvailable;
    }

    clickSpinButton() {
        this.Coupling.data.players = this.players;
        this.$location.path(this.tribe._id + "/pairAssignments/new");
    }

}

angular.module("coupling.controllers")
    .controller('PrepareController', PrepareController);

angular.module("coupling.directives")
    .directive('prepare', () => {
        return {
            controller: 'PrepareController',
            controllerAs: 'prepare',
            bindToController: true,
            scope: {
                tribe: '=',
                players: '='
            },
            restrict: 'E',
            templateUrl: '/app/components/prepare/prepare.html'
        }
    });