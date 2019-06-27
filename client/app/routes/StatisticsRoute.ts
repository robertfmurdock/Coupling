import IRoute = angular.route.IRoute;

const statisticsRoute: IRoute = {
    template: '<statistics tribe-id="main.tribeId" />',
    controller: ['$route', function ($route) {
        this.tribeId = $route.current.params.tribeId;
    }],
    controllerAs: 'main'
};

export default statisticsRoute;