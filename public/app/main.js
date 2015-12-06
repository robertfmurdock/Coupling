/// <reference path="../../typescript-libraries/typings/tsd.d.ts" />
angular.module('coupling.animations', ['ngAnimate']);
/// <reference path="../../typescript-libraries/typings/tsd.d.ts" />
var Player = (function () {
    function Player() {
    }
    return Player;
})();
var CouplingData = (function () {
    function CouplingData() {
    }
    return CouplingData;
})();
var Pin = (function () {
    function Pin() {
    }
    return Pin;
})();
var SelectablePlayer = (function () {
    function SelectablePlayer(isSelected, player) {
        this.isSelected = isSelected;
        this.player = player;
    }
    return SelectablePlayer;
})();
var Coupling = (function () {
    function Coupling($http, $q, $resource) {
        this.$http = $http;
        this.$q = $q;
        this.Tribe = $resource('/api/tribes/:tribeId', { tribeId: '@_id' });
        this.PairAssignmentSet = $resource('/api/:tribeId/history/:id', { id: '@_id' });
        this.data = new CouplingData();
        this.data.selectablePlayers = {};
    }
    Coupling.prototype.getTribes = function () {
        var url = '/api/tribes';
        var self = this;
        return this.Tribe
            .query()
            .$promise
            .catch(function (response) {
            console.info(response);
            return self.$q.reject(Coupling.errorMessage('GET ' + url, response.data, response.status));
        });
    };
    Coupling.prototype.getTribe = function (tribeId) {
        return this.Tribe.get({ tribeId: tribeId })
            .$promise;
    };
    Coupling.prototype.getHistory = function (tribeId) {
        return this.PairAssignmentSet
            .query({ tribeId: tribeId })
            .$promise;
    };
    Coupling.prototype.spin = function (players, tribeId) {
        var url = '/api/' + tribeId + '/spin';
        return this.$http.post(url, players)
            .then(function (result) {
            return result.data;
        }, this.logAndRejectError('POST ' + url));
    };
    Coupling.prototype.saveCurrentPairAssignments = function (tribeId, pairAssignments) {
        var url = '/api/' + tribeId + '/history';
        return this.$http.post(url, pairAssignments)
            .then(function (result) {
            return result.data;
        }, this.logAndRejectError('POST ' + url));
    };
    Coupling.prototype.getPlayers = function (tribeId) {
        var url = '/api/' + tribeId + '/players';
        var self = this;
        return this.$http.get(url)
            .then(function (response) {
            return response.data;
        }, function (response) {
            var data = response.data;
            var statusCode = response.status;
            var message = Coupling.errorMessage(url, data, statusCode);
            console.error('ALERT!\n' + message);
            return self.$q.reject(message);
        });
    };
    Coupling.prototype.savePlayer = function (player) {
        return this.post('/api/' + player.tribe + '/players', player);
    };
    Coupling.prototype.removePlayer = function (player) {
        return this.httpDelete('/api/' + player.tribe + '/players/' + player._id);
    };
    Coupling.prototype.getSelectedPlayers = function (players, history) {
        var _this = this;
        var selectablePlayers = _.map(players, function (player) {
            var selected = _this.playerShouldBeSelected(player, history);
            return [player._id, new SelectablePlayer(selected, player)];
        });
        this.data.selectablePlayers = _.object(selectablePlayers);
        return this.data.selectablePlayers;
    };
    Coupling.prototype.getPins = function (tribeId) {
        var url = '/api/' + tribeId + '/pins';
        var self = this;
        return this.$http.get(url)
            .then(function (response) {
            return response.data;
        }, function (response) {
            var data = response.data;
            var status = response.status;
            return self.$q.reject(Coupling.errorMessage('GET ' + url, data, status));
        });
    };
    Coupling.errorMessage = function (url, data, statusCode) {
        return "There was a problem with request " + url + "\n" +
            "Data: <" + data + ">\n" +
            "Status: " + statusCode;
    };
    Coupling.prototype.logAndRejectError = function (url) {
        var self = this;
        return function (response) {
            var data = response.data;
            var statusCode = response.status;
            var message = Coupling.errorMessage(url, data, statusCode);
            console.error('ALERT!\n' + message);
            return self.$q.reject(message);
        };
    };
    Coupling.prototype.post = function (url, object) {
        return this.$http.post(url, object)
            .then(function (result) {
            return result.data;
        }, this.logAndRejectError('POST ' + url));
    };
    Coupling.prototype.httpDelete = function (url) {
        return this.$http.delete(url)
            .then(function () {
        }, this.logAndRejectError(url));
    };
    Coupling.prototype.isInLastSetOfPairs = function (player, history) {
        var result = _.find(history[0].pairs, function (pairset) {
            if (_.findWhere(pairset, {
                _id: player._id
            })) {
                return true;
            }
        });
        return !!result;
    };
    Coupling.prototype.playerShouldBeSelected = function (player, history) {
        if (this.data.selectablePlayers[player._id]) {
            return this.data.selectablePlayers[player._id].isSelected;
        }
        else if (history.length > 0) {
            return this.isInLastSetOfPairs(player, history);
        }
        else {
            return true;
        }
    };
    Coupling.$inject = ['$http', '$q', '$resource'];
    return Coupling;
})();
var Randomizer = (function () {
    function Randomizer() {
    }
    Randomizer.prototype.next = function (maxValue) {
        var floatValue = Math.random() * maxValue;
        return Math.round(floatValue);
    };
    return Randomizer;
})();
angular.module("coupling.services", ['ngResource'])
    .service("Coupling", Coupling)
    .service('randomizer', Randomizer);
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
        return Coupling.getTribe($route.current.params.tribeId);
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
        players: ['$route', '$q', 'Coupling', function ($route, $q, Coupling) {
                var tribeId = $route.current.params.tribeId;
                return $q.all({
                    players: Coupling.getPlayers(tribeId),
                    history: Coupling.getHistory(tribeId)
                }).then(function (options) {
                    options.selectedPlayers = Coupling.getSelectedPlayers(options.players, options.history);
                    return options;
                }).then(function (options) {
                    return options.players;
                });
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
                return Coupling.getPins($route.current.params.tribeId);
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
                return Coupling.getPlayers($route.current.params.tribeId);
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
                return Coupling.getPlayers($route.current.params.tribeId);
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
                return Coupling.getPlayers($route.current.params.tribeId);
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
                var tribeId = $route.current.params.tribeId;
                return $q.all({
                    tribe: Coupling.getTribe(tribeId),
                    players: Coupling.getPlayers(tribeId),
                    history: Coupling.getHistory(tribeId)
                })
                    .then(function (options) {
                    var players = options['players'];
                    var history = options['history'];
                    var selectablePlayerMap = Coupling.getSelectedPlayers(players, history);
                    options['selectedPlayers'] = _.chain(_.values(selectablePlayerMap))
                        .filter(function (selectable) {
                        return selectable.isSelected;
                    })
                        .map(function (selectable) {
                        return selectable.player;
                    })
                        .value();
                    return options;
                })
                    .then(function (options) {
                    var selectedPlayers = options['selectedPlayers'];
                    options['pairAssignments'] = Coupling.spin(selectedPlayers, tribeId);
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
/// <reference path="../../../../typescript-libraries/typings/tsd.d.ts" />
angular.module("coupling.directives")
    .directive('history', function () {
    return {
        scope: {
            tribe: '=',
            history: '='
        },
        restrict: 'E',
        templateUrl: '/app/components/history/history.html'
    };
});
/// <reference path="../../../../typescript-libraries/typings/tsd.d.ts" />
/// <reference path="../../services.ts" />
var PairAssignmentsController = (function () {
    function PairAssignmentsController(Coupling, $location) {
        this.Coupling = Coupling;
        this.$location = $location;
    }
    Object.defineProperty(PairAssignmentsController.prototype, "unpairedPlayers", {
        get: function () {
            if (this._unpairedPlayers) {
                return this._unpairedPlayers;
            }
            else {
                this._unpairedPlayers = this.findUnpairedPlayers(this.players, this.pairAssignments);
                return this._unpairedPlayers;
            }
        },
        enumerable: true,
        configurable: true
    });
    PairAssignmentsController.prototype.save = function () {
        var self = this;
        this.Coupling.saveCurrentPairAssignments(this.tribe._id, this.pairAssignments)
            .then(function () {
            self.$location.path("/" + self.tribe._id + "/pairAssignments/current");
        });
    };
    PairAssignmentsController.prototype.onDrop = function (draggedPlayer, droppedPlayer) {
        var pairWithDraggedPlayer = this.findPairContainingPlayer(draggedPlayer, this.pairAssignments.pairs);
        var pairWithDroppedPlayer = this.findPairContainingPlayer(droppedPlayer, this.pairAssignments.pairs);
        if (pairWithDraggedPlayer != pairWithDroppedPlayer) {
            this.swapPlayers(pairWithDraggedPlayer, draggedPlayer, droppedPlayer);
            this.swapPlayers(pairWithDroppedPlayer, droppedPlayer, draggedPlayer);
        }
    };
    PairAssignmentsController.prototype.findPairContainingPlayer = function (player, pairs) {
        return _.find(pairs, function (pair) {
            return _.findWhere(pair, {
                _id: player._id
            });
        });
    };
    PairAssignmentsController.prototype.swapPlayers = function (pair, swapOutPlayer, swapInPlayer) {
        _.each(pair, function (player, index) {
            if (swapOutPlayer._id === player._id) {
                pair[index] = swapInPlayer;
            }
        });
    };
    PairAssignmentsController.prototype.findUnpairedPlayers = function (players, pairAssignmentDocument) {
        if (!pairAssignmentDocument) {
            return players;
        }
        var currentlyPairedPlayers = _.flatten(pairAssignmentDocument.pairs);
        return _.filter(players, function (value) {
            var found = _.findWhere(currentlyPairedPlayers, { _id: value._id });
            return found == undefined;
        });
    };
    PairAssignmentsController.$inject = ['Coupling', '$location'];
    return PairAssignmentsController;
})();
angular.module('coupling.controllers')
    .controller('PairAssignmentsController', PairAssignmentsController);
angular.module("coupling.directives")
    .directive('pairAssignments', function () {
    return {
        controller: 'PairAssignmentsController',
        controllerAs: 'pairAssignments',
        bindToController: {
            tribe: '=',
            players: '=',
            pairAssignments: '=pairs',
            isNew: '='
        },
        restrict: 'E',
        templateUrl: '/app/components/pair-assignments/pair-assignments.html'
    };
});
/// <reference path="../../../../typescript-libraries/typings/tsd.d.ts" />
angular.module("coupling.directives")
    .directive('pinList', function () {
    return {
        scope: {
            pins: '='
        },
        restrict: 'E',
        templateUrl: '/app/components/pin-list/pin-list.html'
    };
});
/// <reference path="../../../../typescript-libraries/typings/tsd.d.ts" />
/// <reference path="../../services.ts" />
var PlayerCardController = (function () {
    function PlayerCardController($location) {
        this.$location = $location;
        if (!this.size) {
            this.size = 100;
        }
    }
    PlayerCardController.prototype.clickPlayerName = function ($event) {
        if ($event.stopPropagation)
            $event.stopPropagation();
        this.$location.path("/" + this.player.tribe + "/player/" + this.player._id);
    };
    PlayerCardController.$inject = ['$location'];
    return PlayerCardController;
})();
angular.module('coupling.controllers')
    .controller('PlayerCardController', PlayerCardController);
angular.module("coupling.directives")
    .directive('playercard', function () {
    return {
        templateUrl: '/app/components/player-card/playercard.html',
        restrict: 'E',
        controller: 'PlayerCardController',
        controllerAs: 'playerCard',
        scope: {
            player: '=',
            size: '=?'
        },
        bindToController: true
    };
});
/// <reference path="../../../../typescript-libraries/typings/tsd.d.ts" />
/// <reference path="../../services.ts" />
var PlayerConfigController = (function () {
    function PlayerConfigController($scope, Coupling, $location, $route) {
        this.Coupling = Coupling;
        this.$location = $location;
        this.$route = $route;
        $scope.$on('$locationChangeStart', this.askUserToSave($scope, Coupling));
    }
    PlayerConfigController.prototype.savePlayer = function () {
        this.Coupling.savePlayer(this.player);
        this.$route.reload();
    };
    PlayerConfigController.prototype.removePlayer = function () {
        if (confirm("Are you sure you want to delete this player?")) {
            var self = this;
            this.Coupling
                .removePlayer(this.player)
                .then(function () { return self.navigateToCurrentPairAssignments(); });
        }
    };
    PlayerConfigController.prototype.askUserToSave = function ($scope, Coupling) {
        var self = this;
        return function () {
            if ($scope.playerForm.$dirty) {
                var answer = confirm("You have unsaved data. Would you like to save before you leave?");
                if (answer) {
                    Coupling.savePlayer(self.player);
                }
            }
        };
    };
    PlayerConfigController.prototype.navigateToCurrentPairAssignments = function () {
        this.$location.path("/" + this.tribe._id + "/pairAssignments/current");
    };
    PlayerConfigController.$inject = ['$scope', 'Coupling', '$location', '$route'];
    return PlayerConfigController;
})();
angular.module("coupling.controllers")
    .controller('PlayerConfigController', PlayerConfigController);
angular.module("coupling.directives")
    .directive('playerConfig', function () {
    return {
        controller: 'PlayerConfigController',
        controllerAs: 'playerConfig',
        bindToController: true,
        scope: {
            player: '=',
            players: '=',
            tribe: '='
        },
        restrict: 'E',
        templateUrl: '/app/components/player-config/player-config.html'
    };
});
/// <reference path="../../../../typescript-libraries/typings/tsd.d.ts" />
angular.module("coupling.directives")
    .directive('playerRoster', function () {
    return {
        scope: {
            tribe: '=',
            players: '=',
            label: '=?'
        },
        restrict: 'E',
        templateUrl: '/app/components/player-roster/player-roster.html'
    };
});
/// <reference path="../../../../typescript-libraries/typings/tsd.d.ts" />
/// <reference path="../../services.ts" />
var PrepareController = (function () {
    function PrepareController($location, Coupling) {
        this.$location = $location;
        this.Coupling = Coupling;
        this.selectablePlayers = _.values(Coupling.data.selectablePlayers);
    }
    PrepareController.prototype.clickPlayerCard = function (selectable) {
        selectable.isSelected = !selectable.isSelected;
    };
    PrepareController.prototype.clickSpinButton = function () {
        this.$location.path(this.tribe._id + "/pairAssignments/new");
    };
    PrepareController.$inject = ['$location', 'Coupling'];
    return PrepareController;
})();
angular.module("coupling.controllers")
    .controller('PrepareController', PrepareController);
angular.module("coupling.directives")
    .directive('prepare', function () {
    return {
        controller: 'PrepareController',
        controllerAs: 'prepare',
        bindToController: true,
        scope: {
            tribe: '=',
            players: '='
        },
        restrict: 'E',
        templateUrl: '/app/components/prepare/prepare.html'
    };
});
/// <reference path="../../../../typescript-libraries/typings/tsd.d.ts" />
/// <reference path="../../services.ts" />
var TribeCardController = (function () {
    function TribeCardController($location) {
        this.$location = $location;
    }
    TribeCardController.prototype.clickOnTribeCard = function () {
        this.$location.path("/" + this.tribe._id + "/pairAssignments/current");
    };
    TribeCardController.prototype.clickOnTribeName = function ($event) {
        if ($event.stopPropagation)
            $event.stopPropagation();
        this.$location.path("/" + this.tribe._id + '/edit/');
    };
    TribeCardController.$inject = ['$location'];
    return TribeCardController;
})();
angular.module('coupling.controllers')
    .controller('TribeCardController', TribeCardController);
angular.module("coupling.directives")
    .directive('tribecard', function () {
    return {
        controller: 'TribeCardController',
        controllerAs: 'tribecard',
        scope: {
            tribe: '='
        },
        bindToController: true,
        restrict: 'E',
        templateUrl: '/app/components/tribe-card/tribe-card.html'
    };
});
/// <reference path="../../../../typescript-libraries/typings/tsd.d.ts" />
/// <reference path="../../services.ts" />
var TribeConfigController = (function () {
    function TribeConfigController($location) {
        this.$location = $location;
    }
    TribeConfigController.prototype.clickSaveButton = function () {
        var self = this;
        this.tribe
            .$save()
            .then(function () {
            self.$location.path("/tribes");
        });
    };
    TribeConfigController.$inject = ['$location'];
    return TribeConfigController;
})();
angular.module("coupling.controllers")
    .controller('TribeConfigController', TribeConfigController);
angular.module("coupling.directives")
    .directive('tribeConfig', function () {
    return {
        controller: 'TribeConfigController',
        controllerAs: 'self',
        bindToController: true,
        scope: {
            tribe: '=tribe',
            isNew: '=isNew'
        },
        restrict: 'E',
        templateUrl: '/app/components/tribe-config/tribe-config.html'
    };
});
/// <reference path="../../../../typescript-libraries/typings/tsd.d.ts" />
angular.module("coupling.directives")
    .directive('tribelist', function () {
    return {
        scope: {
            tribes: '='
        },
        restrict: 'E',
        templateUrl: '/app/components/tribe-list/tribe-list.html'
    };
});
/// <reference path="../../../../typescript-libraries/typings/tsd.d.ts" />
/// <reference path="../../services.ts" />
var candidates = [{
        leftCard: {
            name: 'Frodo',
            imagePath: 'frodo-icon.png'
        },
        rightCard: {
            name: 'Sam',
            imagePath: 'samwise-icon.png'
        },
        proverb: 'Together, climb mountains.'
    }, {
        leftCard: {
            name: 'Batman',
            imagePath: 'grayson-icon.png'
        },
        rightCard: {
            name: 'Robin',
            imagePath: 'wayne-icon.png'
        },
        proverb: 'Clean up the city, together.'
    }, {
        leftCard: {
            name: 'Rosie',
            imagePath: 'rosie-icon.png'
        },
        rightCard: {
            name: 'Wendy',
            imagePath: 'wendy-icon.png'
        },
        proverb: 'Team up. Get things done.'
    }];
var WelcomeController = (function () {
    function WelcomeController($timeout, randomizer) {
        this.show = false;
        var choice = WelcomeController.chooseWelcomeCards(randomizer);
        this.leftCard = choice.leftCard;
        this.rightCard = choice.rightCard;
        this.proverb = choice.proverb;
        var self = this;
        $timeout(function () {
            self.show = true;
        }, 0);
    }
    WelcomeController.chooseWelcomeCards = function (randomizer) {
        var indexToUse = randomizer.next(candidates.length - 1);
        return candidates[indexToUse];
    };
    WelcomeController.$inject = ['$timeout', 'randomizer'];
    return WelcomeController;
})();
angular.module('coupling.controllers')
    .controller('WelcomeController', WelcomeController);
angular.module("coupling.directives")
    .directive('welcomepage', function () {
    return {
        restrict: 'E',
        controller: 'WelcomeController',
        controllerAs: 'welcome',
        templateUrl: '/app/components/welcome/welcome.html',
    };
});
/// <reference path="../../typescript-libraries/typings/tsd.d.ts" />
var filters = angular.module("coupling.filters", []);
filters.filter('gravatarUrl', ['gravatarService', function (gravatarService) {
        return function (player, options) {
            if (player && player.imageURL) {
                return player.imageURL;
            }
            else {
                options['default'] = "retro";
                var email = "";
                if (player) {
                    email = player.email ? player.email : player.name;
                }
                return gravatarService.url(email, options);
            }
        };
    }]);
filters.filter('tribeImageUrl', ['gravatarService', function (gravatarService) {
        return function (tribe, options) {
            if (tribe) {
                if (tribe.imageURL) {
                    return tribe.imageURL;
                }
                else if (tribe.email) {
                    options['default'] = "identicon";
                    return gravatarService.url(tribe.email, options);
                }
            }
            return "/images/icons/tribes/no-tribe.png";
        };
    }]);
//# sourceMappingURL=main.js.map