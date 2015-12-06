/// <reference path="../../../../typescript-libraries/typings/tsd.d.ts" />
/// <reference path="../../services.ts" />

class PrepareController {
    static $inject = ['$location', 'Coupling'];

    players:[Player];
    selectablePlayers:SelectablePlayer[];
    tribe:Tribe;

    constructor(private $location:angular.ILocationService, private Coupling:Coupling) {
        this.selectablePlayers = _.values(Coupling.data.selectablePlayers);
    }

    clickPlayerCard(selectable:SelectablePlayer) {
        selectable.isSelected = !selectable.isSelected;
    }

    clickSpinButton() {
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