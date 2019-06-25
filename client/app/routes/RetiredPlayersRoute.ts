import IRoute = angular.route.IRoute;
import {retiredPlayersResolution, tribeResolution} from "./Resolutions";

const retiredPlayersRoute: IRoute = {
    template: '<retired-players tribe-id="main.tribeId">',
    controller: ['$route', function ($route) {
        this.tribeId = $route.current.params.tribeId;
    }],
    controllerAs: 'main'
};

export default retiredPlayersRoute;