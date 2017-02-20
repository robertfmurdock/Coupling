import IRoute = angular.route.IRoute;
import {tribeResolution, playersResolution} from "./Resolutions";

const currentPairAssignmentsRoute: IRoute = {
    template: '<pair-assignments tribe="$resolve.tribe" players="$resolve.players" pairs="$resolve.pairAssignments">',
    resolve: {
        pairAssignments: ['$route', 'Coupling', function ($route, Coupling) {
            return Coupling.getHistory($route.current.params.tribeId).then(function (history) {
                return history[0];
            });
        }],
        tribe: tribeResolution,
        players: playersResolution,
    },

};

export default currentPairAssignmentsRoute;