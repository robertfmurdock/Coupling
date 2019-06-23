import {module} from "angular";
import {Coupling} from "../../services";
import Tribe from "../../../../common/Tribe";
import {connectReactToNg} from "../ReactNgAdapter";
import ReactTribeConfig from "./ReactTribeConfig";

export class TribeConfigController {
    static $inject = ['$location', 'Coupling', '$scope', '$element'];
    public tribe: Tribe;
    public isNew: boolean;
    public styles: any;
    public pairingRules;

    constructor(public $location: angular.ILocationService, public Coupling: Coupling, public $scope, $element?) {
        connectReactToNg({
            component: ReactTribeConfig,
            props: () => ({
                tribe: this.tribe,
                coupling: this.Coupling,
                isNew: this.isNew
            }),
            domNode: $element[0],
            $scope: $scope,
            watchExpression: "tribe",
            $location: $location
        });
    }

}

export default module("coupling.tribeConfig", [])
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
            template: '<div />'
        }
    });