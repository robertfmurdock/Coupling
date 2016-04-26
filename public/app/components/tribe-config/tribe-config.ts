import * as services from '../../services'

export class TribeConfigController {
    static $inject = ['$location'];
    public tribe:services.Tribe;
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

export default angular.module("coupling.tribeConfig", [])
    .controller('TribeConfigController', TribeConfigController)
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