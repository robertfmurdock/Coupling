"use strict";
var controllers = angular.module('coupling.controllers', ['coupling.services']);

controllers.controller('CouplingController', ['$scope', '$location', 'Coupling', function (scope, location, Coupling) {
    scope.data = Coupling.data;
    scope.deselectionMap = [];

    scope.spin = function () {
        location.path("/pairAssignments/new");
    };
    scope.viewPlayer = function (id, $event) {
        if ($event.stopPropagation) $event.stopPropagation();
        location.path("/player/" + id);
    };

    scope.flipSelection = function (player) {
        scope.deselectionMap[player._id] = !scope.deselectionMap[player._id];
    }
}]);

controllers.controller('NewPairAssignmentsController', ['$scope', '$location', 'Coupling', function (scope, location, Coupling) {
    var selectedPlayers = _.filter(Coupling.data.players, function (player) {
        return !scope.deselectionMap[player._id];
    });
    Coupling.spin(selectedPlayers);

    scope.save = function () {
        Coupling.saveCurrentPairAssignments();
        location.path("/pairAssignments/current");
    }
}]);

controllers.controller('CurrentPairAssignmentsController', ['$scope', 'Coupling', function (scope, Coupling) {
    scope.data.currentPairAssignments = Coupling.data.history[0];
}]);

controllers.controller('NewPlayerController', ['$scope', 'Coupling', '$location', function (scope, Coupling, location) {
    scope.player = {};
    scope.savePlayer = function () {
        Coupling.savePlayer(scope.player, function (updatedPlayer) {
            location.path("/player/" + updatedPlayer._id);
        });
    }
}]);

controllers.controller('EditPlayerController', ['$scope', 'Coupling', '$routeParams', function (scope, Coupling, params) {
    Coupling.findPlayerById(params.id, function (player) {
        scope.original = player;
        scope.player = angular.copy(player);
    });

    scope.savePlayer = function () {
        Coupling.savePlayer(scope.player);
    };

    scope.$on('$locationChangeStart', function () {
        if (!angular.equals(scope.original, scope.player)) {
            var answer = confirm("You have unsaved data. Would you like to save before you leave?");
            if (answer) {
                Coupling.savePlayer(scope.player);
            }
        }
    });

}]);