import {tribeResolution} from "./Resolutions";
import IRoute = angular.route.IRoute;

class EditTribeRouteController {
    static $inject = ['tribe'];

    constructor(public tribe) {
    }
}

const editTribeRoute: IRoute = {
    template: '<tribe-config tribe="main.tribe" is-new=false>',
    controllerAs: 'main',
    controller: EditTribeRouteController,
    resolve: {
        tribe: tribeResolution
    }
};

export default editTribeRoute;