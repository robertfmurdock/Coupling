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

    scope.flipSelection = function () {
        scope.deselectionMap[player._id] = !scope.deselectionMap[player._id];
    }
}]);

var formatDate = function (date) {
    return date.getMonth() + 1 + '/' + date.getDate() + "/" + date.getFullYear() + "  (created at " + [date.getHours(),
        date.getMinutes(),
        date.getSeconds()].join(':') + ")";
};

function makeUpdateScopeWithPairAssignmentsFunction(scope, Coupling) {
    return function () {
        scope.formattedDate = formatDate(new Date(Coupling.currentPairAssignments.date));
        scope.pairAssignmentDocument = Coupling.currentPairAssignments;
    };
}

controllers.controller('NewPairAssignmentsController', ['$scope', '$location', 'Coupling', function (scope, location, Coupling) {
    var putPairAssignmentDocumentOnScope = makeUpdateScopeWithPairAssignmentsFunction(scope, Coupling);

    var selectedPlayers = _.filter(Coupling.data.players, function (player) {
        return !scope.deselectionMap[player._id];
    });
    Coupling.spin(selectedPlayers, putPairAssignmentDocumentOnScope);

    scope.save = function () {
        Coupling.saveCurrentPairAssignments(putPairAssignmentDocumentOnScope);
        location.path("/pairAssignments/current");
    }
}]);

controllers.controller('CurrentPairAssignmentsController', ['$scope', 'Coupling', function (scope, Coupling) {
    var putPairAssignmentDocumentOnScope = makeUpdateScopeWithPairAssignmentsFunction(scope, Coupling);
    Coupling.getHistory(function (history) {
        Coupling.currentPairAssignments = history[0];
        putPairAssignmentDocumentOnScope();
    });
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
    scope.player = _.findWhere(Coupling.data.players, {_id: params.id});
    scope.savePlayer = function () {
        Coupling.savePlayer(scope.player);
    }
}]);