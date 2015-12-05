/// <reference path="../../typescript-libraries/typings/tsd.d.ts" />
var app = angular.module('coupling', ["ngRoute", 'ngFitText', 'ui.gravatar', 'ang-drag-drop', 'coupling.controllers', 'coupling.filters', 'coupling.directives', 'coupling.animations']);
app.config(['$locationProvider', function ($locationProvider) {
    $locationProvider.html5Mode({
        enabled: true,
        requireBase: false
    });
}]);
var TribeListRouteController = (function () {
    function TribeListRouteController(tribes) {
        this.tribes = tribes;
    }
    TribeListRouteController.$inject = ['tribes'];
    return TribeListRouteController;
})();
var tribeListRoute = {
    template: '<tribelist tribes="main.tribes">',
    controllerAs: 'main',
    controller: TribeListRouteController,
    resolve: {
        tribes: ['Coupling', function (Coupling) {
            return Coupling.getTribes();
        }]
    }
};
var NewTribeRouteController = (function () {
    function NewTribeRouteController(Coupling) {
        this.tribe = new Coupling.Tribe();
        this.tribe.name = 'New Tribe';
    }
    NewTribeRouteController.$inject = ['Coupling'];
    return NewTribeRouteController;
})();
var newTribeRoute = {
    template: '<tribe-config tribe="main.tribe" is-new=true>',
    controllerAs: 'main',
    controller: NewTribeRouteController
};
var tribeResolution = ['$route', 'Coupling', function ($route, Coupling) {
    return Coupling.requestSpecificTribe($route.current.params.tribeId);
}];
var PrepareTribeRouteController = (function () {
    function PrepareTribeRouteController(tribe, players) {
        this.tribe = tribe;
        this.players = players;
    }
    PrepareTribeRouteController.$inject = ['tribe', 'players'];
    return PrepareTribeRouteController;
})();
var prepareTribeRoute = {
    template: '<prepare tribe="main.tribe" players="main.players">',
    controllerAs: 'main',
    controller: PrepareTribeRouteController,
    resolve: {
        tribe: tribeResolution,
        players: ['$route', 'Coupling', function ($route, Coupling) {
            return Coupling.requestPlayersPromise($route.current.params.tribeId, Coupling.requestHistoryPromise($route.current.params.tribeId));
        }]
    }
};
var EditTribeRouteController = (function () {
    function EditTribeRouteController(tribe) {
        this.tribe = tribe;
    }
    EditTribeRouteController.$inject = ['tribe'];
    return EditTribeRouteController;
})();
var editTribeRoute = {
    template: '<tribe-config tribe="main.tribe" is-new=false>',
    controllerAs: 'main',
    controller: EditTribeRouteController,
    resolve: {
        tribe: tribeResolution
    }
};
app.config(['$routeProvider', function (routeProvider /*:ng.route.IRouteProvider*/) {
    routeProvider.when('/', { redirectTo: '/tribes/' }).when('/tribes/', tribeListRoute).when('/new-tribe/', newTribeRoute).when('/:tribeId/', {
        redirectTo: '/:tribeId/pairAssignments/current/'
    }).when('/:tribeId/prepare/', prepareTribeRoute).when('/:tribeId/edit/', editTribeRoute).when('/:tribeId/history/', {
        template: '<history tribe="main.tribe" history="main.history">',
        controllerAs: 'main',
        controller: ['tribe', 'history', function (tribe, history) {
            this.tribe = tribe;
            this.history = history;
        }],
        resolve: {
            tribe: tribeResolution,
            history: ['$route', 'Coupling', function ($route, Coupling) {
                return Coupling.requestHistoryPromise($route.current.params.tribeId);
            }]
        }
    }).when('/:tribeId/pins', {
        template: '<pin-list pins="main.pins">',
        controllerAs: 'main',
        controller: ['pins', function (pins) {
            this.pins = pins;
        }],
        resolve: {
            pins: ['$route', 'Coupling', function ($route, Coupling) {
                return Coupling.promisePins($route.current.params.tribeId);
            }]
        }
    }).when('/:tribeId/pairAssignments/current/', {
        template: '<pair-assignments tribe="main.tribe" players="main.players" pairs="main.currentPairAssignments" unpaired-players="main.unpairedPlayers">',
        controller: "CurrentPairAssignmentsController",
        controllerAs: 'main',
        resolve: {
            pairAssignmentDocument: ['$route', 'Coupling', function ($route, Coupling) {
                return Coupling.requestHistoryPromise($route.current.params.tribeId).then(function (history) {
                    return history[0];
                });
            }],
            tribe: tribeResolution,
            players: ['$route', 'Coupling', function ($route, Coupling) {
                return Coupling.requestPlayersPromise($route.current.params.tribeId, Coupling.requestHistoryPromise($route.current.params.tribeId));
            }]
        }
    }).when('/:tribeId/pairAssignments/new/', {
        template: '<pair-assignments tribe="main.tribe" players="main.players" pairs="main.currentPairAssignments" unpaired-players="main.unpairedPlayers" save="main.save" on-drop="main.onDrop">',
        controllerAs: 'main',
        controller: "NewPairAssignmentsController",
        resolve: {
            tribe: tribeResolution,
            players: ['$route', 'Coupling', function ($route, Coupling) {
                return Coupling.requestPlayersPromise($route.current.params.tribeId, Coupling.requestHistoryPromise($route.current.params.tribeId));
            }]
        }
    }).when('/:tribeId/player/new/', {
        template: '<player-config>',
        controller: ['$scope', 'tribe', 'players', function ($scope, tribe, players) {
            $scope.tribe = tribe;
            $scope.players = players;
            $scope.player = {
                tribe: tribe._id
            };
        }],
        resolve: {
            tribe: tribeResolution,
            players: ['$route', 'Coupling', function ($route, Coupling) {
                return Coupling.requestPlayersPromise($route.current.params.tribeId, Coupling.requestHistoryPromise($route.current.params.tribeId));
            }]
        }
    }).when('/:tribeId/player/:id/', {
        template: '<player-config>',
        controller: ['$scope', '$route', 'tribe', 'players', function ($scope, $route, tribe, players) {
            $scope.tribe = tribe;
            $scope.players = players;
            var playerId = $route.current.params.id;
            $scope.player = _.findWhere(players, { _id: playerId });
        }],
        resolve: {
            tribe: tribeResolution,
            players: ['$route', 'Coupling', function ($route, Coupling) {
                return Coupling.requestPlayersPromise($route.current.params.tribeId, Coupling.requestHistoryPromise($route.current.params.tribeId));
            }]
        }
    }).when('/auth/google', {
        redirectTo: '/auth/google'
    });
}]);
angular.module('ui.gravatar').config([
    'gravatarServiceProvider',
    function (gravatarServiceProvider) {
        gravatarServiceProvider.defaults = {
            size: 100,
            "default": 'mm'
        };
        gravatarServiceProvider.secure = true;
    }
]);
angular.module('coupling.controllers', ['coupling.services']);
angular.module("coupling.directives", []);
//# sourceMappingURL=app.js.map