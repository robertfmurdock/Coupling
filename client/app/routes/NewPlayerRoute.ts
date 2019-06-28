import IRoute = angular.route.IRoute;

const newPlayerRoute: IRoute = {
    template: '<player-config tribe-id="self.tribeId" />',
    controller: ['$route', function ($route) {
        this.tribeId = $route.current.params.tribeId;
    }],
    controllerAs: 'self',
};

export default newPlayerRoute;