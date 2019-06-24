import IRoute = angular.route.IRoute;

const prepareTribeRoute: IRoute = {
    template: '<prepare tribe-id="main.tribeId" />',
    controller:['$route', function($route) {
        this.tribeId = $route.current.params.tribeId;
    }],
    controllerAs:'main'
};

export default prepareTribeRoute;