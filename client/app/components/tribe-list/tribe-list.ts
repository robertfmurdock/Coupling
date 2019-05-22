import {module} from "angular";
import * as styles from "./styles.css";

import ReactTribeList from "./ReactTribeList"
import {connectReactToNg} from "../ReactNgAdapter";

export default module("coupling.tribeList", [])
    .directive('tribelist', function () {
        return {
            controller: ['$element', '$scope', '$location', function ($element, $scope, $location) {
                this.styles = styles;
                connectReactToNg({
                    component: ReactTribeList,
                    props: () => ({
                        tribes: this.tribes
                    }),
                    domNode: $element[0],
                    $scope: $scope,
                    watchExpression: "tribes",
                    $location: $location
                });
            }],
            controllerAs: 'tribeList',
            bindToController: true,
            scope: {
                tribes: '='
            },
            restrict: 'E',
            template: "<div/>"
        }
    });