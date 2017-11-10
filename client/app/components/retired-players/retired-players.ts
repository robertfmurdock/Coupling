import {module} from "angular";
import * as template from "./retired-players.pug";
import * as Styles from "./styles.css";
import Tribe from "../../../../common/Tribe";

export class RetiredPlayersController {
    public styles;
    tribe: Tribe;

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
                retiredPlayers: '=',
                tribe: '='
            },
            restrict: 'E',
            template: template
        }
    });