/// <reference path="../../../../typescript-libraries/typings/tsd.d.ts" />
/// <reference path="../../services.ts" />
var TribeCardController = (function () {
    function TribeCardController($location) {
        this.$location = $location;
    }
    TribeCardController.prototype.clickOnTribeCard = function () {
        this.$location.path("/" + this.tribe._id + "/pairAssignments/current");
    };
    TribeCardController.prototype.clickOnTribeName = function ($event) {
        if ($event.stopPropagation)
            $event.stopPropagation();
        this.$location.path("/" + this.tribe._id + '/edit/');
    };
    TribeCardController.$inject = ['$location'];
    return TribeCardController;
})();
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
    };
});
//# sourceMappingURL=tribe-card.js.map