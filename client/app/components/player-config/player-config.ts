import * as angular from "angular";
import * as services from "../../services";
import * as template from "./player-config.pug";
import Tribe from "../../../../common/Tribe";
import Badge from "../../../../common/Badge";
import * as merge from "ramda/src/merge";
import IRouteService = angular.route.IRouteService;
import IDirectiveFactory = angular.IDirectiveFactory;

export class PlayerConfigController {
    static $inject = ['$scope', 'Coupling', '$location', '$route'];

    player: services.Player;
    tribe: Tribe;

    constructor($scope,
                public Coupling: services.Coupling,
                public $location: angular.ILocationService,
                public $route: IRouteService) {
        $scope.$on('$locationChangeStart', this.askUserToSave($scope, Coupling));
        $scope.Badge = Badge;
    }

    $onInit() {
        this.player = merge({badge: Badge.Default}, this.player);
    }

    savePlayer() {
        this.Coupling.savePlayer(this.player)
            .then(() => this.$route.reload());
    }

    removePlayer() {
        if (confirm("Are you sure you want to delete this player?")) {
            const self = this;
            this.Coupling
                .removePlayer(this.player)
                .then(() => self.navigateToCurrentPairAssignments());
        }
    }

    private askUserToSave($scope, Coupling) {
        let promptIsAlreadyUp = false;

        return () => {
            if ($scope.playerForm.$dirty && !promptIsAlreadyUp) {
                promptIsAlreadyUp = true;
                const answer = confirm("You have unsaved data. Would you like to save before you leave?");
                if (answer) {
                    Coupling.savePlayer(this.player);
                }
            }
        };
    }

    private navigateToCurrentPairAssignments() {
        this.$location.path(`/${this.tribe.id}/pairAssignments/current`);
    }
}


export default angular.module("coupling.playerConfig", [])
    .controller('PlayerConfigController', PlayerConfigController)
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
            template: template
        }
    });