/// <reference path="../../../../typescript-libraries/typings/tsd.d.ts" />
/// <reference path="../../services.ts" />
var PlayerConfigController = (function () {
    function PlayerConfigController($scope, Coupling, $location, $route) {
        this.Coupling = Coupling;
        this.$location = $location;
        this.$route = $route;
        $scope.$on('$locationChangeStart', this.askUserToSave($scope, Coupling));
    }
    PlayerConfigController.prototype.savePlayer = function () {
        this.Coupling.savePlayer(this.player);
        this.$route.reload();
    };
    PlayerConfigController.prototype.removePlayer = function () {
        if (confirm("Are you sure you want to delete this player?")) {
            this.Coupling.removePlayer(this.player).then(this.navigateToCurrentPairAssignments());
        }
    };
    PlayerConfigController.prototype.askUserToSave = function ($scope, Coupling) {
        var self = this;
        return function () {
            if ($scope.playerForm.$dirty) {
                var answer = confirm("You have unsaved data. Would you like to save before you leave?");
                if (answer) {
                    Coupling.savePlayer(self.player);
                }
            }
        };
    };
    PlayerConfigController.prototype.navigateToCurrentPairAssignments = function () {
        var self = this;
        return function () {
            self.$location.path("/" + self.tribe._id + "/pairAssignments/current");
        };
    };
    PlayerConfigController.$inject = ['$scope', 'Coupling', '$location', '$route'];
    return PlayerConfigController;
})();
angular.module("coupling.controllers").controller('PlayerConfigController', PlayerConfigController);
angular.module("coupling.directives").directive('playerConfig', function () {
    return {
        controller: 'PlayerConfigController',
        controllerAs: 'playerConfig',
        bindToController: true,
        scope: {
            player: '=',
            players: '=',
            tribe: '='
        },
        restrict: 'E',
        templateUrl: '/app/components/player-config/player-config.html'
    };
});
//# sourceMappingURL=player-config.js.map