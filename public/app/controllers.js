"use strict";
var couplingControllers = angular.module('coupling.controllers', ['coupling.services']);

couplingControllers.controller('NewPairAssignmentsController',
  ['$scope', '$location', 'Coupling', '$routeParams', 'tribe', 'players',
    function ($scope, $location, Coupling, $routeParams, tribe, players) {
      $scope.tribe = tribe;

      var selectedPlayers = _.filter(players, function (player) {
        return player.isAvailable;
      });
      Coupling.spin(selectedPlayers, tribe._id)
        .then(function (pairAssignments) {
          $scope.currentPairAssignments = pairAssignments;
          $scope.unpairedPlayers = findUnpairedPlayers(players, pairAssignments);
        });

      $scope.save = function () {
        Coupling.saveCurrentPairAssignments(tribe._id, $scope.currentPairAssignments)
          .then(function () {
            $location.path("/" + $routeParams.tribeId + "/pairAssignments/current");
          });
      };

      function findPairContainingPlayer(player) {
        return _.find($scope.currentPairAssignments.pairs, function (pair) {
          return _.findWhere(pair, {
            _id: player._id
          });
        });
      }

      function swapPlayers(pair, swapOutPlayer, swapInPlayer) {
        _.each(pair, function (player, index) {
          if (swapOutPlayer._id === player._id) {
            pair[index] = swapInPlayer;
          }
        });
      }

      $scope.onDrop = function ($event, draggedPlayer, droppedPlayer) {
        var pairWithDraggedPlayer = findPairContainingPlayer(draggedPlayer);
        var pairWithDroppedPlayer = findPairContainingPlayer(droppedPlayer);

        if (pairWithDraggedPlayer != pairWithDroppedPlayer) {
          swapPlayers(pairWithDraggedPlayer, draggedPlayer, droppedPlayer);
          swapPlayers(pairWithDroppedPlayer, droppedPlayer, draggedPlayer);
        }
      }
    }
  ]);

function findUnpairedPlayers(players, pairAssignmentDocument) {
  if (!pairAssignmentDocument) {
    return players;
  }
  var currentlyPairedPlayers = _.flatten(pairAssignmentDocument.pairs);
  return _.filter(players, function (value) {
    var found = _.findWhere(currentlyPairedPlayers, {_id: value._id});
    return found == undefined;
  });
}
couplingControllers.controller('CurrentPairAssignmentsController',
  ['$scope', 'pairAssignmentDocument', 'tribe', 'players', function ($scope, pairAssignmentDocument, tribe, players) {
    $scope.tribe = tribe;
    $scope.players = players;
    $scope.currentPairAssignments = pairAssignmentDocument;
    $scope.unpairedPlayers = findUnpairedPlayers(players, pairAssignmentDocument)
  }]);
