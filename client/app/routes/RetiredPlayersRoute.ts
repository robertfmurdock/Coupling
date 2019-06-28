import IRoute = angular.route.IRoute;

const retiredPlayersRoute: IRoute = {
    template: '<retired-players tribe-id="main.tribeId">',
    controller: ['$route', function ($route) {
        this.tribeId = $route.current.params.tribeId;
    }],
    controllerAs: 'main'
};

export default retiredPlayersRoute;