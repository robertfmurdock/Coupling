import IRoute = angular.route.IRoute;
import {retiredPlayersResolution, tribeResolution} from "./Resolutions";

const retiredPlayersRoute: IRoute = {
    template: '<retired-players retired-players="$resolve.retiredPlayers" tribe="$resolve.tribe">',
    resolve: {
        retiredPlayers: retiredPlayersResolution,
        tribe: tribeResolution
    }
};

export default retiredPlayersRoute;