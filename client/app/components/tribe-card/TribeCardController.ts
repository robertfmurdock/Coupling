import * as template from './tribe-card.pug'
import * as services from '../../services'

export class TribeCardController {
    static $inject = ['$location'];
    public tribe:services.Tribe;
    size:number;

    constructor(public $location:angular.ILocationService) {
        if (!this.size) {
            this.size = 150;
        }
    }

    clickOnTribeCard() {
        this.$location.path("/" + this.tribe.id + "/pairAssignments/current");
    }

    clickOnTribeName($event) {
        if ($event.stopPropagation) $event.stopPropagation();
        this.$location.path("/" + this.tribe.id + '/edit/');
    }
}

export default angular.module('coupling.tribeCard', [])
    .controller('TribeCardController', TribeCardController)
    .directive('tribecard', function () {
        return {
            controller: 'TribeCardController',
            controllerAs: 'tribecard',
            scope: {
                tribe: '=',
                size: '=?'
            },
            bindToController: true,
            restrict: 'E',
            template: template
        }
    });