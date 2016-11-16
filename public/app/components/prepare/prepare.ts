import * as services from '../../services'
import * as _ from 'underscore'

class PrepareController {
    static $inject = ['$location', 'Coupling'];

    players:[services.Player];
    selectablePlayers:services.SelectablePlayer[];
    tribe:services.Tribe;

    constructor(private $location:angular.ILocationService, private Coupling:services.Coupling) {
        this.selectablePlayers = _.values(Coupling.data.selectablePlayers);
    }

    clickPlayerCard(selectable:services.SelectablePlayer) {
        selectable.isSelected = !selectable.isSelected;
    }

    clickSpinButton() {
        this.$location.path(this.tribe.id + "/pairAssignments/new");
    }

}

export default angular.module("coupling.prepare", [])
    .controller('PrepareController', PrepareController)
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