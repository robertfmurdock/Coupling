import {module} from "angular";
import * as template from "./retired-players.pug";
import * as Styles from "./styles.css";

export class RetiredPlayersController {
    public styles;

    $onInit() {
        this.styles = Styles;
    }
}

export default module("coupling.retiredPlayers", [])
    .directive('retiredPlayers', () => {
        return {
            controllerAs: 'self',
            controller: RetiredPlayersController,
            scope: {
                retiredPlayers: '='
            },
            restrict: 'E',
            template: template
        }
    });