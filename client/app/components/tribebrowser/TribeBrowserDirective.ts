import {module} from "angular";
import * as styles from "./styles.css";
import {connectReactToNg} from "../ReactNgAdapter";
import ReactTribeBrowser from './ReactTribeBrowser'

export default module('coupling.tribebrowser', [])
    .directive('tribebrowser', function () {
        return {
            restrict: 'E',
            controller: ['$element', '$scope', '$location', function ($element, $scope, $location) {
                this.styles = styles;
                connectReactToNg({
                    component: ReactTribeBrowser,
                    props: () => ({
                        tribe: this.tribe
                    }),
                    domNode: $element[0],
                    $scope: $scope,
                    watchExpression: "tribe",
                    $location: $location
                });
            }],
            controllerAs: 'tribebrowser',
            bindToController: {
                tribe: '=',
            },
            template: "<div/>",
        }
    });