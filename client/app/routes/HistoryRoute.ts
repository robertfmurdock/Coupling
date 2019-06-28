import IRoute = angular.route.IRoute;

const historyRoute: IRoute = {
    template: '<history tribe-id="main.tribeId" />',
    controller: ['$route', function ($route) {
        this.tribeId = $route.current.params.tribeId;
    }],
    controllerAs: 'main'
};

export default historyRoute;