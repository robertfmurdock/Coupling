import {tribeResolution} from "./Resolutions";
import * as services from "../services";
import IRoute = angular.route.IRoute;

const prepareTribeRoute: IRoute = {
    template: '<prepare tribe="$resolve.tribe" players="$resolve.players">',
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