import "angular"
import "angular-gravatar"
import "angular-route"
import IRoute = ng.route.IRoute
import IRouteProvider = ng.route.IRouteProvider
import IResource = ng.resource.IResource
import "ng-fittext"
import 'prefixfree'
import "angular-native-dragdrop"
import './filters'
import './animations'
import 'components/components'
import 'font-awesome/css/font-awesome.css'

import * as _ from 'underscore'
import * as services from './services'

var app = angular.module('coupling', ["ngRoute",
    'ngFitText',
    'ui.gravatar',
    'ang-drag-drop',
    'coupling.component',
    'coupling.filters',
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

    tribe:services.Tribe;

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
    return Coupling.getTribe($route.current.params.tribeId);
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
        players: ['$route', '$q', 'Coupling', function ($route, $q, Coupling:services.Coupling) {
            var tribeId = $route.current.params.tribeId;
            return $q.all({
                players: Coupling.getPlayers(tribeId),
                history: Coupling.getHistory(tribeId)
            }).then((options:any)=> {
                options.selectedPlayers = Coupling.getSelectedPlayers(options.players, options.history);
                return options;
            }).then(options=> {
                return options.players;
            });
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

    constructor(public tribe:services.Tribe, public history:[services.PairAssignmentSet]) {
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
        pins: ['$route', 'Coupling', function ($route, Coupling:services.Coupling) {
            return Coupling.getPins($route.current.params.tribeId);
        }]
    }
};

class NewPlayerRouteController {
    static $inject = ['tribe', 'players'];
    tribe:services.Tribe;
    player:services.Player;
    players:[services.Player];

    constructor(tribe, players) {
        this.tribe = tribe;
        this.players = players;
        this.player = new services.Player();
        this.player.tribe = tribe.id;
    }
}

var newPlayerRoute:IRoute = {
    template: '<player-config player="main.player" players="main.players" tribe="main.tribe">',
    controller: NewPlayerRouteController,
    controllerAs: 'main',
    resolve: {
        tribe: tribeResolution,
        players: ['$route', 'Coupling', function ($route, Coupling) {
            return Coupling.getPlayers($route.current.params.tribeId);
        }]
    }
};

class EditPlayerRouteController {
    static $inject = ['$route', 'tribe', 'players'];
    tribe:services.Tribe;
    player:services.Player;
    players:[services.Player];

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
            return Coupling.getPlayers($route.current.params.tribeId);
        }]
    }
};

class CurrentPairAssignmentsRouteController {
    static $inject = ['pairAssignmentDocument', 'tribe', 'players'];

    constructor(public pairAssignments:services.PairAssignmentSet, public tribe:services.Tribe, public players:[services.Player]) {
    }
}

var currentPairAssignmentsRoute:IRoute = {
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
            return Coupling.getPlayers($route.current.params.tribeId);
        }]
    }
};

class NewPairAssignmentsRouteController {
    static $inject = ['requirements'];
    tribe:services.Tribe;
    players:[services.Player];
    pairAssignments:services.PairAssignmentSet;

    constructor(requirements) {
        this.tribe = requirements.tribe;
        this.players = requirements.players;
        this.pairAssignments = requirements.pairAssignments;
    }
}

var newPairAssignmentsRoute:IRoute = {
    template: '<pair-assignments tribe="main.tribe" players="main.players" pairs="main.pairAssignments" is-new="true">',
    controllerAs: 'main',
    controller: NewPairAssignmentsRouteController,
    resolve: {
        requirements: ['$route', '$q', 'Coupling', function ($route:ng.route.IRouteService, $q:angular.IQService, Coupling:services.Coupling) {
            var tribeId = $route.current.params.tribeId;
            return $q.all({
                tribe: Coupling.getTribe(tribeId),
                players: Coupling.getPlayers(tribeId),
                history: Coupling.getHistory(tribeId)
            })
                .then(options=> {
                    var players:[services.Player] = options['players'];
                    var history = options['history'];
                    var selectablePlayerMap = Coupling.getSelectedPlayers(players, history);
                    options['selectedPlayers'] = _.chain(_.values(selectablePlayerMap))
                        .filter(selectable=> {
                            return selectable.isSelected;
                        })
                        .map(selectable=> {
                            return selectable.player;
                        })
                        .value();
                    return options;
                })
                .then(options=> {
                    var selectedPlayers = options['selectedPlayers'];
                    options['pairAssignments'] = Coupling.spin(selectedPlayers, tribeId);
                    return $q.all(options);
                });
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

