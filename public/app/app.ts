/// <reference path="../../typescript-libraries/typings/tsd.d.ts" />
/// <reference path="services.ts" />

import IRoute = ng.route.IRoute
import IRouteProvider = ng.route.IRouteProvider
import IResource = ng.resource.IResource

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

class TribeListRouteController {
    static $inject = ['tribes'];

    constructor(public tribes) {
    }
}

var tribeListRoute:IRoute = {
    template: '<tribelist tribes="main.tribes">',
    controllerAs: 'main',
    controller: TribeListRouteController,
    resolve: {
        tribes: ['Coupling', function (Coupling) {
            return Coupling.getTribes();
        }]
    }
};

class NewTribeRouteController {
    static $inject = ['Coupling'];

    tribe:Tribe;

    constructor(Coupling) {
        this.tribe = new Coupling.Tribe();
        this.tribe.name = 'New Tribe'
    }

}
var newTribeRoute:IRoute = {
    template: '<tribe-config tribe="main.tribe" is-new=true>',
    controllerAs: 'main',
    controller: NewTribeRouteController
};

var tribeResolution = ['$route', 'Coupling', function ($route, Coupling) {
    return Coupling.requestSpecificTribe($route.current.params.tribeId);
}];

class PrepareTribeRouteController {
    static $inject = ['tribe', 'players'];

    constructor(public tribe, public players) {
    }
}

var prepareTribeRoute:IRoute = {
    template: '<prepare tribe="main.tribe" players="main.players">',
    controllerAs: 'main',
    controller: PrepareTribeRouteController,
    resolve: {
        tribe: tribeResolution,
        players: ['$route', 'Coupling', function ($route, Coupling) {
            return Coupling.requestPlayersPromise($route.current.params.tribeId,
                Coupling.getHistory($route.current.params.tribeId));
        }]
    }
};

class EditTribeRouteController {
    static $inject = ['tribe'];

    constructor(public tribe) {
    }
}

var editTribeRoute:IRoute = {
    template: '<tribe-config tribe="main.tribe" is-new=false>',
    controllerAs: 'main',
    controller: EditTribeRouteController,
    resolve: {
        tribe: tribeResolution
    }
};

class HistoryRouteController {
    static $inject = ['tribe', 'history'];

    constructor(public tribe:Tribe, public history:[PairSet]) {
    }
}

var historyRoute:IRoute = {
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

class PinRouteController {
    static $inject = ['pins'];

    constructor(public pins) {
    }
}

var pinRoute:IRoute = {
    template: '<pin-list pins="main.pins">',
    controllerAs: 'main',
    controller: PinRouteController,
    resolve: {
        pins: ['$route', 'Coupling', function ($route, Coupling) {
            return Coupling.promisePins($route.current.params.tribeId);
        }]
    }
};

class NewPlayerRouteController {
    static $inject = ['tribe', 'players'];
    tribe:Tribe;
    player:Player;
    players:[Player];

    constructor(tribe, players) {
        this.tribe = tribe;
        this.players = players;
        this.player = new Player();
        this.player.tribe = tribe._id;
    }
}

var newPlayerRoute:IRoute = {
    template: '<player-config player="main.player" players="main.players" tribe="main.tribe">',
    controller: NewPlayerRouteController,
    controllerAs: 'main',
    resolve: {
        tribe: tribeResolution,
        players: ['$route', 'Coupling', function ($route, Coupling) {
            return Coupling.requestPlayersPromise($route.current.params.tribeId,
                Coupling.getHistory($route.current.params.tribeId));
        }]
    }
};

class EditPlayerRouteController {
    static $inject = ['$route', 'tribe', 'players'];
    tribe:Tribe;
    player:Player;
    players:[Player];

    constructor($route, tribe, players) {
        this.tribe = tribe;
        this.players = players;
        var playerId = $route.current.params.id;
        this.player = _.findWhere(this.players, {_id: playerId});
    }
}

var editPlayerRoute:IRoute = {
    template: '<player-config player="main.player" players="main.players" tribe="main.tribe">',
    controller: EditPlayerRouteController,
    controllerAs: 'main',
    resolve: {
        tribe: tribeResolution,
        players: ['$route', 'Coupling', function ($route, Coupling) {
            return Coupling.requestPlayersPromise($route.current.params.tribeId,
                Coupling.getHistory($route.current.params.tribeId));
        }]
    }
};

app.config(['$routeProvider', function (routeProvider:IRouteProvider) {

    routeProvider
        .when('/', {redirectTo: '/tribes/'})
        .when('/tribes/', tribeListRoute)
        .when('/new-tribe/', newTribeRoute)
        .when('/:tribeId/', {
            redirectTo: '/:tribeId/pairAssignments/current/'
        })
        .when('/:tribeId/prepare/', prepareTribeRoute)
        .when('/:tribeId/edit/', editTribeRoute)
        .when('/:tribeId/history/', historyRoute)
        .when('/:tribeId/pins', pinRoute)
        .when('/:tribeId/pairAssignments/current/', {
            template: '<pair-assignments tribe="main.tribe" players="main.players" pairs="main.currentPairAssignments" unpaired-players="main.unpairedPlayers">',
            controller: "CurrentPairAssignmentsController",
            controllerAs: 'main',
            resolve: {
                pairAssignmentDocument: ['$route', 'Coupling', function ($route, Coupling) {
                    return Coupling.getHistory($route.current.params.tribeId).then(function (history) {
                        return history[0];
                    });
                }],
                tribe: tribeResolution,
                players: ['$route', 'Coupling', function ($route, Coupling) {
                    return Coupling.requestPlayersPromise($route.current.params.tribeId,
                        Coupling.getHistory($route.current.params.tribeId));
                }]
            }
        })
        .when('/:tribeId/pairAssignments/new/', {
            template: '<pair-assignments tribe="main.tribe" players="main.players" pairs="main.currentPairAssignments" unpaired-players="main.unpairedPlayers" save="main.save" on-drop="main.onDrop">',
            controllerAs: 'main',
            controller: "NewPairAssignmentsController",
            resolve: {
                tribe: tribeResolution,
                players: ['$route', 'Coupling', function ($route, Coupling) {
                    return Coupling.requestPlayersPromise($route.current.params.tribeId,
                        Coupling.getHistory($route.current.params.tribeId));
                }]
            }
        })
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
