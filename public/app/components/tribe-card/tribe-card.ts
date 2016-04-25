import * as services from '../../services'

class TribeCardController {
    static $inject = ['$location'];
    public tribe:services.Tribe;

    constructor(public $location:angular.ILocationService) {
    }

    clickOnTribeCard() {
        this.$location.path("/" + this.tribe._id + "/pairAssignments/current");
    }

    clickOnTribeName($event) {
        if ($event.stopPropagation) $event.stopPropagation();
        this.$location.path("/" + this.tribe._id + '/edit/');
    }
}

export default angular.module('coupling.tribeCard', [])
    .controller('TribeCardController', TribeCardController)
    .directive('tribecard', function () {
        return {
            controller: 'TribeCardController',
            controllerAs: 'tribecard',
            scope: {
                tribe: '='
            },
            bindToController: true,
            restrict: 'E',
            templateUrl: '/app/components/tribe-card/tribe-card.html'
        }
    });