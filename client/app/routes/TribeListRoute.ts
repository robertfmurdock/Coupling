import IRoute = angular.route.IRoute;
import {tribesResolution} from "./Resolutions";

const tribeListRoute: IRoute = {
    template: '<tribelist tribes="$resolve.tribes">',
    resolve: {
        tribes: tribesResolution
    }
};

export default tribeListRoute;