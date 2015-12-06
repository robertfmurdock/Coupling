/// <reference path="../../typescript-libraries/typings/tsd.d.ts" />
/// <reference path="services.ts" />
function findUnpairedPlayers(players, pairAssignmentDocument) {
    if (!pairAssignmentDocument) {
        return players;
    }
    var currentlyPairedPlayers = _.flatten(pairAssignmentDocument.pairs);
    return _.filter(players, function (value) {
        var found = _.findWhere(currentlyPairedPlayers, { _id: value._id });
        return found == undefined;
    });
}
var NewPairAssignmentsController = (function () {
    function NewPairAssignmentsController($location, Coupling, $routeParams, tribe, players) {
        this.$location = $location;
        this.Coupling = Coupling;
        this.$routeParams = $routeParams;
        this.tribe = tribe;
        this.players = players;
        var selectedPlayers = _.filter(players, function (player) {
            return player.isAvailable;
        });
        var self = this;
        Coupling.spin(selectedPlayers, tribe._id).then(function (pairAssignments) {
            self.currentPairAssignments = pairAssignments;
            self.unpairedPlayers = self.findUnpairedPlayers(players, pairAssignments);
        });
    }
    NewPairAssignmentsController.prototype.save = function () {
        var self = this;
        this.Coupling.saveCurrentPairAssignments(this.tribe._id, this.currentPairAssignments).then(function () {
            self.$location.path("/" + self.$routeParams['tribeId'] + "/pairAssignments/current");
        });
    };
    NewPairAssignmentsController.prototype.onDrop = function (draggedPlayer, droppedPlayer) {
        var pairWithDraggedPlayer = this.findPairContainingPlayer(draggedPlayer, this.currentPairAssignments.pairs);
        var pairWithDroppedPlayer = this.findPairContainingPlayer(droppedPlayer, this.currentPairAssignments.pairs);
        if (pairWithDraggedPlayer != pairWithDroppedPlayer) {
            this.swapPlayers(pairWithDraggedPlayer, draggedPlayer, droppedPlayer);
            this.swapPlayers(pairWithDroppedPlayer, droppedPlayer, draggedPlayer);
        }
    };
    NewPairAssignmentsController.prototype.findUnpairedPlayers = function (players, pairAssignmentDocument) {
        if (!pairAssignmentDocument) {
            return players;
        }
        var currentlyPairedPlayers = _.flatten(pairAssignmentDocument.pairs);
        return _.filter(players, function (value) {
            var found = _.findWhere(currentlyPairedPlayers, { _id: value._id });
            return found == undefined;
        });
    };
    NewPairAssignmentsController.prototype.findPairContainingPlayer = function (player, pairs) {
        return _.find(pairs, function (pair) {
            return _.findWhere(pair, {
                _id: player._id
            });
        });
    };
    NewPairAssignmentsController.prototype.swapPlayers = function (pair, swapOutPlayer, swapInPlayer) {
        _.each(pair, function (player, index) {
            if (swapOutPlayer._id === player._id) {
                pair[index] = swapInPlayer;
            }
        });
    };
    NewPairAssignmentsController.$inject = ['$location', 'Coupling', '$routeParams', 'tribe', 'players'];
    return NewPairAssignmentsController;
})();
var CurrentPairAssignmentsController = (function () {
    function CurrentPairAssignmentsController(currentPairAssignments, tribe, players) {
        this.currentPairAssignments = currentPairAssignments;
        this.tribe = tribe;
        this.players = players;
        this.unpairedPlayers = findUnpairedPlayers(players, currentPairAssignments);
    }
    CurrentPairAssignmentsController.$inject = ['pairAssignmentDocument', 'tribe', 'players'];
    return CurrentPairAssignmentsController;
})();
angular.module('coupling.controllers').controller('NewPairAssignmentsController', NewPairAssignmentsController).controller('CurrentPairAssignmentsController', CurrentPairAssignmentsController);
//# sourceMappingURL=controllers.js.map