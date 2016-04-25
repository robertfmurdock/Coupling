
/// <reference path="../../services.ts" />


class TribeConfigController {
    static $inject = ['$location'];
    public tribe:Tribe;
    public isNew:boolean;

    constructor(public $location: angular.ILocationService) {
    }

    clickSaveButton() {
        var self = this;
        this.tribe
            .$save()
            .then(() => {
                self.$location.path("/tribes");
            });
    }
}

angular.module("coupling.controllers")
    .controller('TribeConfigController', TribeConfigController);

angular.module("coupling.directives")
    .directive('tribeConfig', function () {
        return {
            controller: 'TribeConfigController',
            controllerAs: 'self',
            bindToController: true,
            scope: {
                tribe: '=tribe',
                isNew: '=isNew'
            },
            restrict: 'E',
            templateUrl: '/app/components/tribe-config/tribe-config.html'
        }
    });