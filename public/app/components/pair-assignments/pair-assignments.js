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
//# sourceMappingURL=pair-assignments.js.map