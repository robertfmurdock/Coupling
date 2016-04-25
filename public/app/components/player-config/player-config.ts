
/// <reference path="../../services.ts" />

class PlayerConfigController {
    static $inject = ['$scope', 'Coupling', '$location', '$route'];

    player:Player;
    tribe:Tribe;

    constructor($scope, public Coupling:Coupling, public $location:angular.ILocationService, public $route:ng.route.IRouteService) {
        $scope.$on('$locationChangeStart', this.askUserToSave($scope, Coupling));
    }

    savePlayer() {
        this.Coupling.savePlayer(this.player)
            .then(()=> {
                this.$route.reload();
            });
    }

    removePlayer() {
        if (confirm("Are you sure you want to delete this player?")) {
            var self = this;
            this.Coupling
                .removePlayer(this.player)
                .then(()=>self.navigateToCurrentPairAssignments());
        }
    }

    private askUserToSave($scope, Coupling) {
        var self = this;
        return () => {
            if ($scope.playerForm.$dirty) {
                var answer = confirm("You have unsaved data. Would you like to save before you leave?");
                if (answer) {
                    Coupling.savePlayer(self.player);
                }
            }
        };
    }

    private navigateToCurrentPairAssignments() {
        this.$location.path("/" + this.tribe._id + "/pairAssignments/current");
    }
}

angular.module("coupling.controllers")
    .controller('PlayerConfigController', PlayerConfigController);

angular.module("coupling.directives")
    .directive('playerConfig', () => {
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
        }
    });