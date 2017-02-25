import {module} from "angular";
import * as template from "./tribe-list.pug";
import * as styles from "./styles.css";

export default module("coupling.tribeList", [])
    .directive('tribelist', function () {
        return {
            controller: function () {
                this.styles = styles;
            },
            controllerAs: 'tribeList',
            bindToController: true,
            scope: {
                tribes: '='
            },
            restrict: 'E',
            template: template
        }
    });