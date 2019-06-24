import IRoute = angular.route.IRoute;

const newTribeRoute: IRoute = {
    template: '<tribe-config />',
    controllerAs: 'self',
    controller: [function () {
        this.tribe = {};
        this.tribe.name = 'New Tribe'
    }]
};

export default newTribeRoute;