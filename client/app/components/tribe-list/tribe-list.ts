import {module} from "angular";
import * as styles from "./styles.css";

import ReactTribeList from "./ReactTribeList"
import {connectReactToNg} from "../ReactNgAdapter";
import TribeListPage from "../tribe-list-page/TribeListPage";

export default module("coupling.tribeList", [])
    .directive('tribelist', function () {
        return {
            controller: ['$element', '$scope', '$location', 'Coupling', function ($element, $scope, $location, coupling) {
                this.styles = styles;
                connectReactToNg({
                    component: TribeListPage,
                    props: () => ({coupling}),
                    domNode: $element[0],
                    $scope: $scope,
                    watchExpression: "",
                    $location: $location
                });
            }],
            restrict: 'E',
            template: "<div/>"
        }
    });