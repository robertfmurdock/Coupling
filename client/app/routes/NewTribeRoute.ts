import IRoute = angular.route.IRoute;
import * as services from "../services";

class NewTribeRouteController {
    static $inject = ['Coupling'];

    tribe: services.Tribe;

    constructor(Coupling) {
        this.tribe = new Coupling.Tribe();
        this.tribe.name = 'New Tribe'
    }

}
const newTribeRoute: IRoute = {
    template: '<tribe-config tribe="main.tribe" is-new=true>',
    controllerAs: 'main',
    controller: NewTribeRouteController
};

export default newTribeRoute;