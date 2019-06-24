import {module} from "angular";
import {Coupling} from "../../services";
import {connectReactToNg} from "../ReactNgAdapter";
import TribeConfigPage from "./TribeConfigPage";

export class TribeConfigController {
    static $inject = ['$location', 'Coupling', '$scope', '$element'];
    public tribeId: string;
    public isNew: boolean;

    constructor($location: angular.ILocationService, coupling: Coupling, $scope, $element) {
        connectReactToNg({
            component: TribeConfigPage,
            props: () => ({
                tribeId: this.tribeId,
                isNew: this.isNew,
                coupling: coupling
            }),
            domNode: $element[0],
            $scope: $scope,
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
                tribeId: '=tribeId'
            },
            restrict: 'E',
            template: '<div />'
        }
    });