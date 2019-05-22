import {module} from "angular";
import * as template from "./template.pug";
import * as styles from "./styles.css";

import ReactTribeBrowser from './ReactTribeBrowser'

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