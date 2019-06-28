import {module} from "angular";
import * as template from "./pin-list.pug";
import {connectReactToNg} from "../ReactNgAdapter";
import TribeConfigPage from "../tribe-config/TribeConfigPage";
import PinPage from "./PinPage";

export default module("coupling.pinList", [])
    .directive('pinList', () => {
        return {
            scope: {
                tribeId: '='
            },
            bindToController: true,
            restrict: 'E',
            controller: ['$location', 'Coupling', '$scope', '$element', function ($location, coupling, $scope, $element) {
                connectReactToNg({
                    component: PinPage,
                    props: () => ({
                        tribeId: this.tribeId,
                        coupling: coupling
                    }),
                    domNode: $element[0],
                    $scope: $scope,
                    $location: $location
                });
            }]
        }
    });