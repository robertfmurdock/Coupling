import IRoute = angular.route.IRoute;
import * as services from '../services';
import {tribeResolution} from "./Resolutions";

class CurrentPairAssignmentsRouteController {
    static $inject = ['pairAssignmentDocument', 'tribe', 'players'];

    constructor(public pairAssignments:services.PairAssignmentSet, public tribe:services.Tribe, public players:[services.Player]) {
    }
}

const currentPairAssignmentsRoute:IRoute = {
    template: '<pair-assignments tribe="main.tribe" players="main.players" pairs="main.pairAssignments">',
    controller: CurrentPairAssignmentsRouteController,
    controllerAs: 'main',
    resolve: {
        pairAssignmentDocument: ['$route', 'Coupling', function ($route, Coupling) {
            return Coupling.getHistory($route.current.params.tribeId).then(function (history) {
                return history[0];
            });
        }],
        tribe: tribeResolution,
        players: ['$route', 'Coupling', function ($route, Coupling) {
            return Coupling.getPlayers($route.current.params.tribeId);
        }]
    }
};

export default currentPairAssignmentsRoute;