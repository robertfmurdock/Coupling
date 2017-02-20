import {tribeResolution, historyResolution} from "./Resolutions";
import IRoute = angular.route.IRoute;

const historyRoute: IRoute = {
    template: '<history tribe="$resolve.tribe" history="$resolve.history">',
    resolve: {
        tribe: tribeResolution,
        history: historyResolution
    }
};

export default historyRoute;