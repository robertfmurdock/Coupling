/// <reference path="../../../../typescript-libraries/typings/tsd.d.ts" />
/// <reference path="../../services.ts" />
var PrepareController = (function () {
    function PrepareController($location, Coupling) {
        this.$location = $location;
        this.Coupling = Coupling;
    }
    PrepareController.prototype.clickPlayerCard = function (player) {
        player.isAvailable = !player.isAvailable;
    };
    PrepareController.prototype.clickSpinButton = function () {
        this.Coupling.data.players = this.players;
        this.$location.path(this.tribe._id + "/pairAssignments/new");
    };
    PrepareController.$inject = ['$location', 'Coupling'];
    return PrepareController;
})();
angular.module("coupling.controllers").controller('PrepareController', PrepareController);
angular.module("coupling.directives").directive('prepare', function () {
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
    };
});
//# sourceMappingURL=prepare.js.map