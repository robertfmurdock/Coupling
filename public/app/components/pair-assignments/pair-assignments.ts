import * as services from '../../services'
import '../controllers'
import * as _ from 'underscore'

class PairAssignmentsController {
    static $inject = ['Coupling', '$location'];
    tribe:services.Tribe;
    players:[services.Player];
    pairAssignments:services.PairAssignmentSet;
    isNew:boolean;
    private _unpairedPlayers:services.Player[];

    constructor(public Coupling, private $location) {
    }

    get unpairedPlayers():services.Player[] {
        if (this._unpairedPlayers) {
            return this._unpairedPlayers;
        } else {
            this._unpairedPlayers = this.findUnpairedPlayers(this.players, this.pairAssignments);
            return this._unpairedPlayers;
        }
    }

    save() {
        var self = this;
        this.Coupling.saveCurrentPairAssignments(this.pairAssignments)
            .then(function () {
                self.$location.path("/" + self.tribe._id + "/pairAssignments/current");
            });
    }

    onDrop(draggedPlayer, droppedPlayer) {
        var pairWithDraggedPlayer = this.findPairContainingPlayer(draggedPlayer, this.pairAssignments.pairs);
        var pairWithDroppedPlayer = this.findPairContainingPlayer(droppedPlayer, this.pairAssignments.pairs);

        if (pairWithDraggedPlayer != pairWithDroppedPlayer) {
            this.swapPlayers(pairWithDraggedPlayer, draggedPlayer, droppedPlayer);
            this.swapPlayers(pairWithDroppedPlayer, droppedPlayer, draggedPlayer);
        }
    }

    private findPairContainingPlayer(player, pairs:[[services.Player]]) {
        return _.find(pairs, function (pair) {
            return _.findWhere(pair, {
                _id: player._id
            });
        });
    }


    private swapPlayers(pair, swapOutPlayer, swapInPlayer) {
        _.each(pair, function (player:services.Player, index) {
            if (swapOutPlayer._id === player._id) {
                pair[index] = swapInPlayer;
            }
        });
    }

    private findUnpairedPlayers(players:[services.Player], pairAssignmentDocument:services.PairAssignmentSet):services.Player[] {
        if (!pairAssignmentDocument) {
            return players;
        }
        var currentlyPairedPlayers = _.flatten(pairAssignmentDocument.pairs);
        return _.filter(players, function (value:services.Player) {
            var found = _.findWhere(currentlyPairedPlayers, {_id: value._id});
            return found == undefined;
        });
    }
}

angular.module('coupling.controllers')
    .controller('PairAssignmentsController', PairAssignmentsController);


angular.module("coupling.directives")
    .directive('pairAssignments', () => {
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
        }
    });
