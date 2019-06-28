import IRoute = angular.route.IRoute;

const retiredPlayerRoute: IRoute = {
    template: '<retired-player-config player-id="self.playerId" tribe-id="self.tribeId">',
    controller: ['$route', function ($route) {
        this.tribeId = $route.current.params.tribeId;
        this.playerId = $route.current.params.id;
    }],
    controllerAs: 'self',
};

export default retiredPlayerRoute;