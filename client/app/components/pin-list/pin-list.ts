import {module} from "angular";
import * as template from "./pin-list.pug";

export default module("coupling.pinList", [])
    .directive('pinList', () => {
        return {
            scope: {
                pins: '='
            },
            restrict: 'E',
            template: template
        }
    });