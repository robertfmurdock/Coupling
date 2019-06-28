import * as angular from "angular";
import * as services from "../../services";
import IRouteService = angular.route.IRouteService;
import {connectReactToNg} from "../ReactNgAdapter";
import PlayerPage from "./PlayerPage";

export class PlayerConfigController {
    static $inject = ['$scope', 'Coupling', '$location', '$route', '$element'];
    playerId?: string;
    tribeId: string;

    constructor(public $scope,
                public Coupling: services.Coupling,
                public $location: angular.ILocationService,
                public $route: IRouteService,
                $element?) {
        let locationChangeCallback = () => undefined;

        connectReactToNg({
            component: PlayerPage,
            props: () => ({
                tribeId: this.tribeId,
                playerId: this.playerId,
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
            bindToController: true,
            scope: {
                tribeId: '=',
                playerId: '=',
            },
            restrict: 'E',
            template: '<div />'
        }
    });