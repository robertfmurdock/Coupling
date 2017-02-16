import {tribeResolution} from "./Resolutions";
import IRoute = angular.route.IRoute;
import * as services from '../services';

class HistoryRouteController {
    static $inject = ['tribe', 'history'];

    constructor(public tribe:services.Tribe, public history:[services.PairAssignmentSet]) {
    }
}

const historyRoute:IRoute = {
    template: '<history tribe="main.tribe" history="main.history">',
    controllerAs: 'main',
    controller: HistoryRouteController,
    resolve: {
        tribe: tribeResolution,
        history: ['$route', 'Coupling', function ($route, Coupling) {
            return Coupling.getHistory($route.current.params.tribeId);
        }]
    }
};

export default historyRoute;