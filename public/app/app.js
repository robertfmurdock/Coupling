/// <reference path="../../typescript-libraries/typings/tsd.d.ts" />
/// <reference path="services.ts" />
var app = angular.module('coupling', ["ngRoute",
    'ngFitText',
    'ui.gravatar',
    'ang-drag-drop',
    'coupling.controllers',
    'coupling.filters',
    'coupling.directives',
    'coupling.animations']);
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
                return Coupling.requestPlayersPromise($route.current.params.tribeId, Coupling.getHistory($route.current.params.tribeId));
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
var HistoryRouteController = (function () {
    function HistoryRouteController(tribe, history) {
        this.tribe = tribe;
        this.history = history;
    }
    HistoryRouteController.$inject = ['tribe', 'history'];
    return HistoryRouteController;
})();
var historyRoute = {
    template: '<history tribe="main.tribe" history="main.history">',
    controllerAs: 'main',
    controller: HistoryRouteController,
    resolve: {
        tribe: tribeResolution,
        history: ['$route', 'Coupling', function ($route, Coupling) {
                return Coupling.getHistory($route.current.params.tribeId);
            }]
    }
};
var PinRouteController = (function () {
    function PinRouteController(pins) {
        this.pins = pins;
    }
    PinRouteController.$inject = ['pins'];
    return PinRouteController;
})();
var pinRoute = {
    template: '<pin-list pins="main.pins">',
    controllerAs: 'main',
    controller: PinRouteController,
    resolve: {
        pins: ['$route', 'Coupling', function ($route, Coupling) {
                return Coupling.promisePins($route.current.params.tribeId);
            }]
    }
};
var NewPlayerRouteController = (function () {
    function NewPlayerRouteController(tribe, players) {
        this.tribe = tribe;
        this.players = players;
        this.player = new Player();
        this.player.tribe = tribe._id;
    }
    NewPlayerRouteController.$inject = ['tribe', 'players'];
    return NewPlayerRouteController;
})();
var newPlayerRoute = {
    template: '<player-config player="main.player" players="main.players" tribe="main.tribe">',
    controller: NewPlayerRouteController,
    controllerAs: 'main',
    resolve: {
        tribe: tribeResolution,
        players: ['$route', 'Coupling', function ($route, Coupling) {
                return Coupling.requestPlayersPromise($route.current.params.tribeId, Coupling.getHistory($route.current.params.tribeId));
            }]
    }
};
var EditPlayerRouteController = (function () {
    function EditPlayerRouteController($route, tribe, players) {
        this.tribe = tribe;
        this.players = players;
        var playerId = $route.current.params.id;
        this.player = _.findWhere(this.players, { _id: playerId });
    }
    EditPlayerRouteController.$inject = ['$route', 'tribe', 'players'];
    return EditPlayerRouteController;
})();
var editPlayerRoute = {
    template: '<player-config player="main.player" players="main.players" tribe="main.tribe">',
    controller: EditPlayerRouteController,
    controllerAs: 'main',
    resolve: {
        tribe: tribeResolution,
        players: ['$route', 'Coupling', function ($route, Coupling) {
                return Coupling.requestPlayersPromise($route.current.params.tribeId, Coupling.getHistory($route.current.params.tribeId));
            }]
    }
};
var CurrentPairAssignmentsRouteController = (function () {
    function CurrentPairAssignmentsRouteController(pairAssignments, tribe, players) {
        this.pairAssignments = pairAssignments;
        this.tribe = tribe;
        this.players = players;
    }
    CurrentPairAssignmentsRouteController.$inject = ['pairAssignmentDocument', 'tribe', 'players'];
    return CurrentPairAssignmentsRouteController;
})();
var currentPairAssignmentsRoute = {
    template: '<pair-assignments tribe="main.tribe" players="main.players" pairs="main.pairAssignments">',
    controller: CurrentPairAssignmentsRouteController,
    controllerAs: 'main',
    resolve: {
        pairAssignmentDocument: ['$route', 'Coupling', function ($route, Coupling) {
                return Coupling.getHistory($route.current.params.tribeId).then(function (history) {
                    return history[0];
                });
            }],
        tribe: tribeResolution,
        players: ['$route', 'Coupling', function ($route, Coupling) {
                return Coupling.requestPlayersPromise($route.current.params.tribeId, Coupling.getHistory($route.current.params.tribeId));
            }]
    }
};
var NewPairAssignmentsRouteController = (function () {
    function NewPairAssignmentsRouteController(requirements) {
        this.tribe = requirements.tribe;
        this.players = requirements.players;
        this.pairAssignments = requirements.pairAssignments;
    }
    NewPairAssignmentsRouteController.$inject = ['requirements'];
    return NewPairAssignmentsRouteController;
})();
var newPairAssignmentsRoute = {
    template: '<pair-assignments tribe="main.tribe" players="main.players" pairs="main.pairAssignments" is-new="true">',
    controllerAs: 'main',
    controller: NewPairAssignmentsRouteController,
    resolve: {
        requirements: ['$route', '$q', 'Coupling', function ($route, $q, Coupling) {
                return $q.all({
                    tribe: Coupling.requestSpecificTribe($route.current.params.tribeId),
                    players: Coupling.requestPlayersPromise($route.current.params.tribeId, Coupling.getHistory($route.current.params.tribeId))
                })
                    .then(function (options) {
                    var players = options['players'];
                    var tribe = options['tribe'];
                    var selectedPlayers = _.filter(players, function (player) {
                        return player.isAvailable;
                    });
                    options['pairAssignments'] = Coupling.spin(selectedPlayers, tribe._id);
                    return $q.all(options);
                });
            }]
    }
};
app.config(['$routeProvider', function (routeProvider) {
        routeProvider
            .when('/', { redirectTo: '/tribes/' })
            .when('/tribes/', tribeListRoute)
            .when('/new-tribe/', newTribeRoute)
            .when('/:tribeId/', {
            redirectTo: '/:tribeId/pairAssignments/current/'
        })
            .when('/:tribeId/prepare/', prepareTribeRoute)
            .when('/:tribeId/edit/', editTribeRoute)
            .when('/:tribeId/history/', historyRoute)
            .when('/:tribeId/pins', pinRoute)
            .when('/:tribeId/pairAssignments/current/', currentPairAssignmentsRoute)
            .when('/:tribeId/pairAssignments/new/', newPairAssignmentsRoute)
            .when('/:tribeId/player/new/', newPlayerRoute)
            .when('/:tribeId/player/:id/', editPlayerRoute)
            .when('/auth/google', {
            redirectTo: '/auth/google'
        });
    }]);
angular.module('ui.gravatar')
    .config([
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