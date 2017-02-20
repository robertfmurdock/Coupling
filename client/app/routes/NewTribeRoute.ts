import IRoute = angular.route.IRoute;
import {Coupling} from "../services";

const newTribeRoute: IRoute = {
    template: '<tribe-config tribe="self.tribe" is-new=true>',
    controllerAs: 'self',
    controller: ['Coupling', function (Coupling: Coupling) {
        this.tribe = new Coupling.Tribe();
        this.tribe.name = 'New Tribe'
    }]
};

export default newTribeRoute;