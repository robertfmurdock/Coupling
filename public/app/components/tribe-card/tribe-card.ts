
/// <reference path="../../services.ts" />

class TribeCardController {
    static $inject = ['$location'];
    public tribe: Tribe;

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

angular.module('coupling.controllers')
    .controller('TribeCardController', TribeCardController);

angular.module("coupling.directives")
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