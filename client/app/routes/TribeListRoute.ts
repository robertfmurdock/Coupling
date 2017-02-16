import IRoute = angular.route.IRoute;

class TribeListRouteController {
    static $inject = ['tribes'];

    constructor(public tribes) {
    }
}

const tribeListRoute:IRoute = {
    template: '<tribelist tribes="main.tribes">',
    controllerAs: 'main',
    controller: TribeListRouteController,
    resolve: {
        tribes: ['Coupling', function (Coupling) {
            return Coupling.getTribes();
        }]
    }
};

export default tribeListRoute;