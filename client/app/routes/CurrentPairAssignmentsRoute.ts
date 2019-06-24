import IRoute = angular.route.IRoute;

const currentPairAssignmentsRoute: IRoute = {
    template: '<current-pair-assignments tribe-id="main.tribeId" />',
    controller: ['$route', function ($route) {
        this.tribeId = $route.current.params.tribeId
    }],
    controllerAs: 'main'
};

export default currentPairAssignmentsRoute;