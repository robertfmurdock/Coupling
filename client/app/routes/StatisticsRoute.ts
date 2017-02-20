import IRoute = angular.route.IRoute;
import {tribeResolution, playersResolution, historyResolution} from "./Resolutions";

const statisticsRoute: IRoute = {
    template: '<statistics tribe="$resolve.tribe" players="$resolve.players" history="$resolve.history">',
    resolve: {
        tribe: tribeResolution,
        players: playersResolution,
        history: historyResolution
    }
};

export default statisticsRoute;