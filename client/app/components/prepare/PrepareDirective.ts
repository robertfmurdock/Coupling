import {module} from "angular";
import {connectReactToNg} from "../ReactNgAdapter";
import PrepareForSpinPage from "./PrepareForSpinPage";

class PrepareController {
    static $inject = ['$location', '$element', '$scope', 'Coupling'];
    tribeId: string;
    constructor(private $location: angular.ILocationService, $element, $scope, coupling) {
        connectReactToNg({
            component: PrepareForSpinPage,
            props: () => ({
                tribeId: this.tribeId,
                coupling: coupling
            }),
            domNode: $element[0],
            $scope: $scope,
            watchExpression: "tribeId",
            $location: $location
        });

    }

}

export default module("coupling.prepare", [])
    .controller('PrepareController', PrepareController)
    .directive('prepare', () => {
        return {
            controller: 'PrepareController',
            controllerAs: 'prepare',
            bindToController: true,
            scope: {
                tribeId: '='
            },
            restrict: 'E',
            template: '<div/>'
        }
    });