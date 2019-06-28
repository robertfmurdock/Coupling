import IRoute = angular.route.IRoute;

const pinRoute: IRoute = {
    template: '<pin-list tribe-id="main.tribeId">',
    controller: ['$route', function ($route) {
        this.tribeId = $route.current.params.tribeId;
    }],
    controllerAs: 'main'
};

export default pinRoute;