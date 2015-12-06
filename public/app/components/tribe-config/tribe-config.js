/// <reference path="../../../../typescript-libraries/typings/tsd.d.ts" />
/// <reference path="../../services.ts" />
var TribeConfigController = (function () {
    function TribeConfigController($location) {
        this.$location = $location;
    }
    TribeConfigController.prototype.clickSaveButton = function () {
        var self = this;
        this.tribe
            .$save()
            .then(function () {
            self.$location.path("/tribes");
        });
    };
    TribeConfigController.$inject = ['$location'];
    return TribeConfigController;
})();
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
    };
});
//# sourceMappingURL=tribe-config.js.map