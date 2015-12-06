/// <reference path="../../typescript-libraries/typings/tsd.d.ts" />
/// <reference path="services.ts" />

function findUnpairedPlayers(players, pairAssignmentDocument:PairSet) {
    if (!pairAssignmentDocument) {
        return players;
    }
    var currentlyPairedPlayers = _.flatten(pairAssignmentDocument.pairs);
    return _.filter(players, function (value:Player) {
        var found = _.findWhere(currentlyPairedPlayers, {_id: value._id});
        return found == undefined;
    });
}

class NewPairAssignmentsController {
    static $inject = ['$location', 'Coupling', '$routeParams', 'tribe', 'players'];

    currentPairAssignments:PairSet;
    unpairedPlayers:[Player];

    constructor(public $location:angular.ILocationService, private Coupling, private $routeParams:angular.route.IRouteParamsService, public tribe, public players:[Player]) {
        var selectedPlayers = _.filter(players, function (player) {
            return player.isAvailable;
        });

        var self = this;

        Coupling.spin(selectedPlayers, tribe._id)
            .then(function (pairAssignments) {
                self.currentPairAssignments = pairAssignments;
                self.unpairedPlayers = self.findUnpairedPlayers(players, pairAssignments);
            });
    }

    save() {
        var self = this;
        this.Coupling.saveCurrentPairAssignments(this.tribe._id, this.currentPairAssignments)
            .then(function () {
                self.$location.path("/" + self.$routeParams['tribeId'] + "/pairAssignments/current");
            });
    }

    onDrop(draggedPlayer, droppedPlayer) {
        var pairWithDraggedPlayer = this.findPairContainingPlayer(draggedPlayer, this.currentPairAssignments.pairs);
        var pairWithDroppedPlayer = this.findPairContainingPlayer(droppedPlayer, this.currentPairAssignments.pairs);

        if (pairWithDraggedPlayer != pairWithDroppedPlayer) {
            this.swapPlayers(pairWithDraggedPlayer, draggedPlayer, droppedPlayer);
            this.swapPlayers(pairWithDroppedPlayer, droppedPlayer, draggedPlayer);
        }
    }

    private findUnpairedPlayers(players, pairAssignmentDocument:PairSet) {
        if (!pairAssignmentDocument) {
            return players;
        }
        var currentlyPairedPlayers = _.flatten(pairAssignmentDocument.pairs);
        return _.filter(players, function (value:Player) {
            var found = _.findWhere(currentlyPairedPlayers, {_id: value._id});
            return found == undefined;
        });
    }

    private findPairContainingPlayer(player, pairs:[[Player]]) {
        return _.find(pairs, function (pair) {
            return _.findWhere(pair, {
                _id: player._id
            });
        });
    }


    private swapPlayers(pair, swapOutPlayer, swapInPlayer) {
        _.each(pair, function (player:Player, index) {
            if (swapOutPlayer._id === player._id) {
                pair[index] = swapInPlayer;
            }
        });
    }
}

class CurrentPairAssignmentsController {
    static $inject = ['pairAssignmentDocument', 'tribe', 'players'];
    unpairedPlayers:[Player];

    constructor(public currentPairAssignments:PairSet, public tribe:Tribe, public players:[Player]) {
        this.unpairedPlayers = findUnpairedPlayers(players, currentPairAssignments)
    }
}

angular.module('coupling.controllers')
    .controller('NewPairAssignmentsController', NewPairAssignmentsController)
    .controller('CurrentPairAssignmentsController', CurrentPairAssignmentsController);
