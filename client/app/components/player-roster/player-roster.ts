import {module} from "angular";
import * as template from "./player-roster.pug";
export default module("coupling.playerRoster", [])
    .directive('playerRoster', () => {
        return {
            scope: {
                tribe: '=',
                players: '=',
                label: '=?'
            },
            restrict: 'E',
            template: template
        }
    });