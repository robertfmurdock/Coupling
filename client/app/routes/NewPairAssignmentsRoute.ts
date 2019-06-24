import IRoute = angular.route.IRoute;

const newPairAssignmentsRoute: IRoute = {
    template: '<new-pair-assignments tribe-id="main.tribeId" player-ids="main.playerIds" />',
    controllerAs: 'main',
    controller: ['$route', '$location', function ($route, $location) {
        this.tribeId = $route.current.params.tribeId;
        this.playerIds = $location.search().player;
    }],
};

export default newPairAssignmentsRoute;