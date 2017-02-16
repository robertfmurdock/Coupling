import {tribeResolution} from "./Resolutions";
import * as services from "../services";
import IRoute = angular.route.IRoute;

class PrepareTribeRouteController {
    static $inject = ['tribe', 'players'];

    constructor(public tribe, public players) {
    }
}

const prepareTribeRoute: IRoute = {
    template: '<prepare tribe="main.tribe" players="main.players">',
    controllerAs: 'main',
    controller: PrepareTribeRouteController,
    resolve: {
        tribe: tribeResolution,
        players: ['$route', '$q', 'Coupling', function ($route, $q, Coupling: services.Coupling) {
            const tribeId = $route.current.params.tribeId;
            return $q.all({
                players: Coupling.getPlayers(tribeId),
                history: Coupling.getHistory(tribeId)
            }).then((options: any) => {
                options.selectedPlayers = Coupling.getSelectedPlayers(options.players, options.history);
                return options;
            }).then(options => {
                return options.players;
            });
        }]
    }
};

export default prepareTribeRoute;