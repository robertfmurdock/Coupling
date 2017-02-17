import IRoute = angular.route.IRoute;
import {tribeResolution} from "./Resolutions";

class StatisticsRouteController {
    static $inject = ['tribe'];

    constructor(public tribe) {
    }
}

const statisticsRoute:IRoute = {
    template: '<statistics tribe="main.tribe">',
    controllerAs: 'main',
    controller: StatisticsRouteController,
    resolve: {
        tribe: tribeResolution
    }
};

export default statisticsRoute;