/// <reference path="../../../../typescript-libraries/typings/tsd.d.ts" />
/// <reference path="../../services.ts" />
var PrepareController = (function () {
    function PrepareController($location, Coupling) {
        this.$location = $location;
        this.Coupling = Coupling;
        this.selectablePlayers = _.values(Coupling.data.selectablePlayers);
    }
    PrepareController.prototype.clickPlayerCard = function (selectable) {
        selectable.isSelected = !selectable.isSelected;
    };
    PrepareController.prototype.clickSpinButton = function () {
        this.$location.path(this.tribe._id + "/pairAssignments/new");
    };
    PrepareController.$inject = ['$location', 'Coupling'];
    return PrepareController;
})();
angular.module("coupling.controllers")
    .controller('PrepareController', PrepareController);
angular.module("coupling.directives")
    .directive('prepare', function () {
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