import * as template from "./statistics.pug";
import Tribe from "../../../../common/Tribe";
import Player from "../../../../common/Player";
import {module} from "angular";
import * as Styles from "./styles.css";
import StatisticComposer from "../../runners/StatisticComposer";
// @ts-ignore
import * as client from 'client'

const commandDispatcher = client.commandDispatcher();


export class StatisticsController {
    public tribe: Tribe;
    public players: Player[];
    public statistics;
    public history;
    public styles;
    public data;
    public activePlayerCount;

    $onInit() {
        const composer = new StatisticComposer();

        this.statistics = composer.compose(this.tribe, this.players, this.history);
        this.styles = Styles;
        this.data = commandDispatcher.performCalculateHeatMapCommand(this.players, this.history, this.statistics.spinsUntilFullRotation);
        this.activePlayerCount = this.players.length;
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