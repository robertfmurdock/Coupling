import IRoute = angular.route.IRoute;

class EditTribeRouteController {
    static $inject = ['$route'];
    private tribeId: string;
    constructor(public $route) {
        this.tribeId = $route.current.params.tribeId
    }
}

const editTribeRoute: IRoute = {
    template: '<tribe-config tribe-id="main.tribeId">',
    controllerAs: 'main',
    controller: EditTribeRouteController,
};

export default editTribeRoute;