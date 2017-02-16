import IRoute = angular.route.IRoute;
import * as services from '../services';

class PinRouteController {
    static $inject = ['pins'];

    constructor(public pins) {
    }
}

const pinRoute:IRoute = {
    template: '<pin-list pins="main.pins">',
    controllerAs: 'main',
    controller: PinRouteController,
    resolve: {
        pins: ['$route', 'Coupling', function ($route, Coupling:services.Coupling) {
            return Coupling.getPins($route.current.params.tribeId);
        }]
    }
};

export default pinRoute;