import IRoute = angular.route.IRoute;
import {tribeResolution, playersResolution, historyResolution} from "./Resolutions";

class StatisticsRouteController {
    static $inject = ['tribe', 'players', 'history'];

    constructor(public tribe, public players, public history) {
    }
}

const statisticsRoute:IRoute = {
    template: '<statistics tribe="main.tribe" players="main.players" history="main.history">',
    controllerAs: 'main',
    controller: StatisticsRouteController,
    resolve: {
        tribe: tribeResolution,
        players: playersResolution,
        history: historyResolution
    }
};

export default statisticsRoute;