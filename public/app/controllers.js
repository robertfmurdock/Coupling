"use strict";
var couplingControllers = angular.module('coupling.controllers', ['coupling.services']);

couplingControllers.controller('WelcomeController', ['$scope', '$timeout', 'randomizer', function ($scope, $timeout, randomizer) {
  $scope.show = false;
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

  var indexToUse = randomizer.next(candidates.length - 1);
  var choice = candidates[indexToUse];

  $scope.leftCard = choice.leftCard;
  $scope.rightCard = choice.rightCard;
  $scope.proverb = choice.proverb;

  $timeout(function () {
    $scope.show = true;
  }, 0);
}]);

couplingControllers.controller('TribeCardController', ['$scope', '$location', function ($scope, $location) {
  $scope.clickOnTribeCard = function (tribe) {
    $location.path("/" + tribe._id + "/pairAssignments/current");
  };
  $scope.clickOnTribeName = function (tribe, $event) {
    if ($event.stopPropagation) $event.stopPropagation();
    $location.path("/" + tribe._id + '/edit/');
  };
}]);

couplingControllers.controller('TribeListController', ['$scope', '$location', 'tribes',
  function ($scope, $location, tribes) {
    $scope.tribes = tribes;
  }]);

couplingControllers.controller('NewTribeController', ['$scope', '$location', 'Coupling', function ($scope, $location, Coupling) {
  $scope.tribe = new Coupling.Tribe();
  $scope.tribe.name = 'New Tribe';

  $scope.clickSaveButton = function () {
    $scope.tribe._id = $scope.tribe.requestedId;
    delete $scope.tribe.requestedId;
    Coupling.saveTribe($scope.tribe).then(function () {
      $location.path("/tribes");
    });
  }
}]);

couplingControllers.controller('EditTribeController', ['$scope', 'Coupling', '$location', 'tribe', function ($scope, Coupling, $location, tribe) {
  $scope.tribe = tribe;
  $scope.clickSaveButton = function () {
    Coupling.saveTribe($scope.tribe).then(function () {
      $location.path("/tribes");
    });
  }
}]);

couplingControllers.controller('HistoryController', ['$scope', 'tribe', 'history', function ($scope, tribe, history) {
  $scope.tribe = tribe;
  $scope.history = history;
}]);

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

couplingControllers.controller('PrepareController', ['$scope', 'tribe', 'players', '$location', 'Coupling',
  function ($scope, tribe, players, $location, Coupling) {
    $scope.tribe = tribe;
    $scope.players = players;
    $scope.clickSpinButton = function () {
      Coupling.data.players = $scope.players;
      $location.path(tribe._id + "/pairAssignments/new");
    };
  }]);

couplingControllers.controller('NewPlayerController',
  ['$scope', 'Coupling', '$location', 'tribe', 'players', function ($scope, Coupling, $location, tribe, players) {
    $scope.tribe = tribe;
    $scope.players = players;
    $scope.player = {
      tribe: tribe._id
    };
    $scope.savePlayer = function () {
      Coupling.savePlayer($scope.player).then(function (updatedPlayer) {
        $location.path("/" + tribe._id + "/player/" + updatedPlayer._id);
      });
    }
  }]);

couplingControllers.controller('EditPlayerController',
  ['$scope', 'Coupling', '$location', '$route', 'tribe', 'players',
    function ($scope, Coupling, $location, $route, tribe, players) {
      $scope.tribe = tribe;
      $scope.players = players;

      var playerId = $route.current.params.id;
      var player = _.findWhere(players, {_id: playerId});

      $scope.original = player;
      $scope.player = angular.copy(player);

      $scope.savePlayer = function () {
        Coupling.savePlayer($scope.player);
        $route.reload();
      };

      $scope.removePlayer = function () {
        if (confirm("Are you sure you want to delete this player?")) {
          Coupling.removePlayer($scope.player).then(function () {
            $location.path("/" + tribe._id + "/pairAssignments/current");
          });
        }
      };

      $scope.$on('$locationChangeStart', function () {
        if ($scope.playerForm.$dirty) {
          var answer = confirm("You have unsaved data. Would you like to save before you leave?");
          if (answer) {
            Coupling.savePlayer($scope.player);
          }
        }
      });
    }]);

couplingControllers.controller('PinListController', ['$scope', 'Coupling', '$routeParams', function ($scope, Coupling, $routeParams) {
  var promisePins = Coupling.promisePins($routeParams.tribeId);
  promisePins.then(function (pins) {
    $scope.pins = pins;
  });
}]);