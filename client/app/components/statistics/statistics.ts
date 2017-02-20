import * as template from './statistics.pug';
import Tribe from "../../../../common/Tribe";
import Player from "../../../../common/Player";
import {module} from "angular";
import StatisticComposer from "../../../../server/lib/StatisticComposer";
import * as Styles from './styles.css';

export class StatisticsController {
    public tribe: Tribe;
    public players: Player[];
    public statistics;
    public history;
    public styles;

    $onInit() {
        const composer = new StatisticComposer();
        this.statistics = composer.compose(this.tribe, this.players, this.history);
        this.styles = Styles;
    }
}

export default module('coupling.statistics', [])
    .directive('statistics', function () {
        return {
            controllerAs: 'self',
            controller: StatisticsController,
            bindToController: true,
            scope: {
                tribe: '=',
                players: '=',
                history: '='
            },
            template: template
        }
    });