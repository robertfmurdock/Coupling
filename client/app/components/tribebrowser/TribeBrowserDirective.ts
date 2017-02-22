import {module} from "angular";
import * as template from "./template.pug";
import * as styles from "./styles.css";

export default module('coupling.tribebrowser', [])
    .directive('tribebrowser', function () {
        return {
            restrict: 'E',
            controller: function () {
                this.styles = styles;
            },
            controllerAs: 'tribebrowser',
            bindToController: {
                tribe: '=',
            },
            template: template,
        }
    });