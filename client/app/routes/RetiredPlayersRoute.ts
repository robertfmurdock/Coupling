import IRoute = angular.route.IRoute;
import {retiredPlayersResolution} from "./Resolutions";

const retiredPlayersRoute: IRoute = {
    template: '<retired-players retired-players="$resolve.retiredPlayers">',
    resolve: {
        retiredPlayers: retiredPlayersResolution
    }
};

export default retiredPlayersRoute;