import * as angular from "angular";
import * as services from "../../services";
import * as template from "./player-config.pug";
import Tribe from "../../../../common/Tribe";
import Player from "../../../../common/Player";
import IRouteService = angular.route.IRouteService;
import {connectReactToNg} from "../ReactNgAdapter";
import ReactPlayerConfig from "./ReactPlayerConfig";

export class PlayerConfigController {
    static $inject = ['$scope', 'Coupling', '$location', '$route', '$element'];
    player: Player;
    players: Player[];
    tribe: Tribe;

    constructor(public $scope,
                public Coupling: services.Coupling,
                public $location: angular.ILocationService,
                public $route: IRouteService,
                $element?) {
        let locationChangeCallback = () => undefined;

        connectReactToNg({
            component: ReactPlayerConfig,
            props: () => ({
                tribe: this.tribe,
                player: this.player,
                players: this.players,
                coupling: this.Coupling,
                locationChanger: (callback) => {
                    locationChangeCallback = callback;
                }
            }),
            domNode: $element[0],
            $scope: $scope,
            watchExpression: "",
            $location: $location
        });

        $scope.$on('$locationChangeStart', () => {
            locationChangeCallback();
        });
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