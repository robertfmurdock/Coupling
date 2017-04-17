import IRoute = angular.route.IRoute;
import {pinsResolution} from "./Resolutions";

const pinRoute: IRoute = {
    template: '<pin-list pins="$resolve.pins">',
    resolve: {
        pins: pinsResolution
    }
};

export default pinRoute;