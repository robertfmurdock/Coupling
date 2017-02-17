import IRoute = angular.route.IRoute;
import {tribeResolution, playersResolution} from "./Resolutions";

class StatisticsRouteController {
    static $inject = ['tribe', 'players'];

    constructor(public tribe, public players) {
    }
}

const statisticsRoute:IRoute = {
    template: '<statistics tribe="main.tribe" players="main.players">',
    controllerAs: 'main',
    controller: StatisticsRouteController,
    resolve: {
        tribe: tribeResolution,
        players: playersResolution
    }
};

export default statisticsRoute;