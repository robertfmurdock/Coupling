"use strict";

describe('The controller named ', function () {

  var scope, location, Coupling;

  beforeEach(function () {
    module("coupling.controllers");
    scope = {};
  });

  xdescribe('SelectedPlayerCardController', function () {

    beforeEach(function () {
      scope.player = {
        name: 'Chad',
        _id: 'PrettyGreatPlayerId',
        isAvailable: true
      };
      inject(function ($controller) {
        $controller('SelectedPlayerCardController', {
          $scope: scope,
          $location: location,
          Coupling: Coupling
        })
      })
    });

    describe('clickPlayerName', function () {
      it('will redirect to the players page', function () {
        var expectedPath = '/' + Coupling.data.selectedTribeId + '/player/' + scope.player._id;
        expect(location.path).not.toHaveBeenCalledWith(expectedPath);
        var event = {};
        scope.clickPlayerName(scope.player._id, event);
        expect(location.path).toHaveBeenCalledWith(expectedPath);
      });

      it('will stop propagation to other click events', function () {
        var event = {
          stopPropagation: jasmine.createSpy('stopPropagation')
        };
        scope.clickPlayerName(event);
        expect(event.stopPropagation).toHaveBeenCalled();
      });
    });

    describe('clickPlayerCard', function () {
      it('will change a players selection in the map', function () {
        expect(scope.player.isAvailable).toBe(true);
        scope.clickPlayerCard();
        expect(scope.player.isAvailable).toBe(false);
        scope.clickPlayerCard();
        expect(scope.player.isAvailable).toBe(true);
      });
    });
  });

  function injectController(controllerName, scope, location, Coupling, routeParams) {
    inject(function ($controller) {
      $controller(controllerName, {
        $scope: scope,
        $location: location,
        Coupling: Coupling,
        $routeParams: routeParams
      });
    });
  }

  function checkControllerWillDeselectTribe(controllerName, Coupling, location, routeParams) {
    expect(Coupling.selectTribe).not.toHaveBeenCalled();
    injectController(controllerName, scope, location, Coupling, routeParams);
    expect(Coupling.selectTribe).toHaveBeenCalledWith(null);
  }

  describe('TribeListController', function () {

    var Coupling, location;
    beforeEach(function () {
      location = {
        path: jasmine.createSpy('path')
      };
      Coupling = {
        data: {},
        selectTribe: jasmine.createSpy('selectTribe')
      };
    });

    function injectTribeListController(expectedTribes) {
      inject(function ($controller) {
        $controller('TribeListController', {
          $scope: scope,
          $location: location,
          tribes: expectedTribes
        });
      });
    }

    it('will get tribes and put them on scope', function () {
      var expectedTribes = [{
        _id: '1'
      }, {
        _id: '2'
      }];
      inject(function ($controller) {
        $controller('TribeListController', {
          $scope: scope,
          $location: location,
          tribes: expectedTribes
        });
      });
      expect(scope.tribes).toBe(expectedTribes);
    });

    describe('scopes a function named', function () {
      beforeEach(function () {
        injectTribeListController([]);
      });

      describe('clickOnTribeCard', function () {
        it('that changes location to that tribe\'s current pair assignments', function () {
          var tribe = {
            _id: 'amazingMagicId'
          };
          expect(location.path).not.toHaveBeenCalled();
          scope.clickOnTribeCard(tribe);
          expect(location.path).toHaveBeenCalledWith("/" + tribe._id + "/pairAssignments/current");
        });
      });

      describe('clickOnTribeName', function () {
        it('that changes location to that tribe', function () {
          var tribe = {
            _id: 'amazingMagicId'
          };
          expect(location.path).not.toHaveBeenCalled();
          scope.clickOnTribeName(tribe);
          expect(location.path).toHaveBeenCalledWith("/" + tribe._id);
        });
      });
    });
  });

  var NewTribeController = 'NewTribeController';
  describe(NewTribeController, function () {

    var Coupling, location;
    beforeEach(function () {
      location = {
        path: jasmine.createSpy('path')
      };
      Coupling = {
        data: {},
        selectTribe: jasmine.createSpy('selectTribe')
      };
    });

    it('creates and selects a new tribe', function () {
      var previouslySelectedTribe = {
        name: 'This should not be the tribe after injection.'
      };
      scope.tribe = previouslySelectedTribe;
      injectController(NewTribeController, scope, location, Coupling);
      expect(scope.tribe).not.toBe(previouslySelectedTribe);
      expect(scope.tribe.name).toBe('New Tribe');
    });

    it('will deselect tribe', function () {
      checkControllerWillDeselectTribe(NewTribeController, Coupling, location);
    });

    describe('when pressing the save button ', function () {
      beforeEach(function () {
        Coupling.saveTribe = jasmine.createSpy('save tribe spy');
        injectController(NewTribeController, scope, location, Coupling);
      });

      it('will use the Coupling service to save the tribe', function () {
        var expectedId = 'importantId';
        scope.tribe.requestedId = expectedId;
        scope.clickSaveButton();

        expect(Coupling.saveTribe).toHaveBeenCalled();
        var saveTribeArgs = Coupling.saveTribe.calls.argsFor(0);
        expect(saveTribeArgs[0]).toBe(scope.tribe);
        expect(saveTribeArgs[0]._id).toBe(expectedId);
        expect(saveTribeArgs[0].requestedId).toBeUndefined();
      });

      describe('when the save is complete', function () {
        var callback;
        beforeEach(function () {
          scope.clickSaveButton();
          callback = Coupling.saveTribe.calls.argsFor(0)[1];
        });

        it('will return to the tribe list', function () {
          var newTribeId = 'expectedId';
          var expectedPath = '/tribes';
          expect(location.path).not.toHaveBeenCalledWith(expectedPath);

          var updatedTribe = {
            _id: newTribeId
          };
          callback(updatedTribe);
          expect(location.path).toHaveBeenCalledWith(expectedPath);
        });
      });
    });
  });


  var EditTribeController = 'EditTribeController';
  describe(EditTribeController, function () {

    var Coupling, location, routeParams;
    var selectTribeDefer = new RSVP.defer();
    var selectedTribeId;
    beforeEach(function () {
      location = {
        path: jasmine.createSpy('path')
      };
      var selectedTribe = {
        name: 'Party tribe.',
        _id: 'party'
      };
      Coupling = {
        data: {
          selectedTribe: selectedTribe
        },
        selectTribe: function (tribeId) {
          selectedTribeId = tribeId;
          return selectTribeDefer.promise;
        }
      };
      routeParams = {
        tribeId: selectedTribe._id
      };
    });

    it('puts the selected tribe on the scope', function () {
      var previouslyScopedTribe = {
        name: 'This should not be the tribe after injection.'
      };
      scope.tribe = previouslyScopedTribe;

      inject(function ($controller) {
        $controller(EditTribeController, {
          $scope: scope,
          $location: location,
          Coupling: Coupling,
          tribe: Coupling.data.selectedTribe
        });
      });

      expect(scope.tribe).not.toBe(previouslyScopedTribe);
      expect(scope.tribe).toBe(Coupling.data.selectedTribe);
    });

    describe('when pressing the save button ', function () {
      beforeEach(function () {
        Coupling.saveTribe = jasmine.createSpy('save tribe spy');
        inject(function ($controller) {
          $controller(EditTribeController, {
            $scope: scope,
            $location: location,
            Coupling: Coupling,
            tribe: Coupling.data.selectedTribe
          });
        });
      });

      it('will use the Coupling service to save the tribe', function () {
        scope.clickSaveButton();

        expect(Coupling.saveTribe).toHaveBeenCalled();
        var saveTribeArgs = Coupling.saveTribe.calls.argsFor(0);
        expect(saveTribeArgs[0]).toBe(scope.tribe);
      });

      describe('when the save is complete', function () {
        var callback;
        beforeEach(function () {
          scope.clickSaveButton();
          callback = Coupling.saveTribe.calls.argsFor(0)[1];
        });

        it('will change the location to the current pair assignments', function () {
          var newTribeId = 'expectedId';
          var expectedPath = '/tribes';
          expect(location.path).not.toHaveBeenCalledWith(expectedPath);

          var updatedTribe = {
            _id: newTribeId
          };
          callback(updatedTribe);
          expect(location.path).toHaveBeenCalledWith(expectedPath);
        });
      });
    });
  });


  var HistoryController = 'HistoryController';
  xdescribe(HistoryController, function () {

    var Coupling, location, routeParams;
    beforeEach(function () {
      location = {
        path: jasmine.createSpy('path')
      };
      var selectedTribe = {
        name: 'Party tribe.',
        _id: 'party'
      };
      Coupling = {
        data: {
          selectedTribe: selectedTribe
        },
        selectTribe: jasmine.createSpy('selectTribe')
      };
      routeParams = {
        tribeId: selectedTribe._id
      };
    });

    it('will select tribe', function () {
      expect(Coupling.selectTribe).not.toHaveBeenCalled();
      injectController(HistoryController, scope, location, Coupling, routeParams);
      expect(Coupling.selectTribe).toHaveBeenCalledWith(Coupling.data.selectedTribe._id);
    });

    it('will minimize the player roster', function () {
      scope.playerRoster.minimized = false;
      injectController(HistoryController, scope, location, Coupling, routeParams);
      expect(scope.playerRoster.minimized).toBe(true);
    });

  });


  xdescribe('NewPairAssignmentsController', function () {
    var ControllerName = 'NewPairAssignmentsController';
    var Coupling, location, routeParams;

    var selectTribeDefer = new RSVP.defer();
    var selectedTribeId;

    beforeEach(function () {
      location = {
        path: jasmine.createSpy('path')
      };
      var selectedTribe = {
        name: 'Party tribe.',
        _id: 'party'
      };
      Coupling = {
        data: {
          selectedTribe: selectedTribe
        },
        selectTribe: function (tribeId) {
          selectedTribeId = tribeId;
          return selectTribeDefer.promise;
        },
        spin: jasmine.createSpy('spin'),
        saveCurrentPairAssignments: jasmine.createSpy('save')
      };
      scope.data = Coupling.data;
      routeParams = {
        tribeId: selectedTribe._id
      };
    });

    it('will select tribe and spin all selected players', function (done) {
      injectController(ControllerName, scope, location, Coupling, routeParams);
      expect(selectedTribeId).toBe(Coupling.data.selectedTribe._id);
      var players = [{
        _id: 'h8',
        isAvailable: false
      }, {
        _id: '3r',
        isAvailable: true
      }, {
        _id: '8d3',
        isAvailable: true
      }];
      selectTribeDefer.resolve({
        players: players
      });
      selectTribeDefer.promise.then(function () {
        expect(Coupling.spin).toHaveBeenCalledWith([players[1], players[2]]);
        done();
      }).catch(done);
    });


    it('save will use Coupling service to save and then will redirect to the current pair assignments page', function () {
      injectController(ControllerName, scope, location, Coupling, routeParams);
      expect(Coupling.saveCurrentPairAssignments).not.toHaveBeenCalled();
      scope.save();
      expect(Coupling.saveCurrentPairAssignments).toHaveBeenCalled();
      expect(location.path).toHaveBeenCalledWith("/" + routeParams.tribeId + "/pairAssignments/current");
    });

    it('onDrop will take two players and swap their places', function () {
      injectController(ControllerName, scope, location, Coupling, routeParams);
      var player1 = {
        _id: '1'
      };
      var player2 = {
        _id: '2'
      };
      var player3 = {
        _id: '3'
      };
      var player4 = {
        _id: '4'
      };

      Coupling.data.currentPairAssignments = {
        pairs: [
          [player1, player2],
          [player3, player4]
        ]
      };

      scope.onDrop(null, player2, player3);
      expect(Coupling.data.currentPairAssignments.pairs).toEqual([
        [player1, player3],
        [player2, player4]
      ]);
    });
    it('onDrop will not swap players that are already paired', function () {
      injectController(ControllerName, scope, location, Coupling, routeParams);
      var player1 = {
        _id: '1'
      };
      var player2 = {
        _id: '2'
      };
      var player3 = {
        _id: '3'
      };
      var player4 = {
        _id: '4'
      };

      Coupling.data.currentPairAssignments = {
        pairs: [
          [player1, player2],
          [player3, player4]
        ]
      };

      scope.onDrop(null, player4, player3);
      expect(Coupling.data.currentPairAssignments.pairs).toEqual([
        [player1, player2],
        [player3, player4]
      ]);
    });
  });

  describe('CurrentPairAssignmentsController', function () {
    var ControllerName = 'CurrentPairAssignmentsController';
    var Coupling, location, routeParams;

    var selectTribeDefer = new RSVP.defer();
    var selectedTribeId;

    var selectedTribe = {
      name: 'Party tribe.',
      _id: 'party'
    };

    beforeEach(function () {
      location = {
        path: jasmine.createSpy('path')
      };
      Coupling = {
        data: {
          selectedTribe: selectedTribe
        },
        selectTribe: function (tribeId) {
          selectedTribeId = tribeId;
          return selectTribeDefer.promise;
        },
        spin: jasmine.createSpy('spin'),
        saveCurrentPairAssignments: jasmine.createSpy('save')
      };
      scope.data = Coupling.data;
      routeParams = {
        tribeId: selectedTribe._id
      };
    });

    it('will select tribe and then select the latest pairs', function () {
      var currentPairs = [
        ['tom', 'jerry']
      ];
      inject(function ($controller) {
        $controller(ControllerName, {
          $scope: scope,
          Coupling: Coupling,
          currentPairs: currentPairs,
          tribe: selectedTribe
        });
      });
      expect(scope.currentPairAssignments).toBe(currentPairs);
    });
  });

  describe('NewPlayerController', function () {
    var ControllerName = 'NewPlayerController';
    var Coupling, location, routeParams;

    var selectedTribe = {
      name: 'Party tribe.',
      _id: 'party'
    };

    beforeEach(function () {
      location = {
        path: jasmine.createSpy('path')
      };

      Coupling = {
        data: {
          selectedTribe: selectedTribe
        },
        selectTribe: jasmine.createSpy('selectTribe'),
        spin: jasmine.createSpy('spin'),
        savePlayer: jasmine.createSpy('save')
      };
      scope.data = Coupling.data;
      routeParams = {
        tribeId: selectedTribe._id
      };
    });

    it('will select tribe', function () {
      inject(function ($controller) {
        $controller(ControllerName, {
          $scope: scope,
          $location: location,
          Coupling: Coupling,
          tribe: selectedTribe
        });
      });
      expect(scope.tribe).toBe(selectedTribe);
    });

    it('will create a new player with the given tribe', function () {
      scope.player = null;
      inject(function ($controller) {
        $controller(ControllerName, {
          $scope: scope,
          $location: location,
          Coupling: Coupling,
          tribe: selectedTribe
        });
      });
      expect(scope.player).toEqual({
        tribe: routeParams.tribeId
      });
    });

    it('can save player using Coupling service and redirects to player page on callback', function () {
      inject(function ($controller) {
        $controller(ControllerName, {
          $scope: scope,
          $location: location,
          Coupling: Coupling,
          tribe: selectedTribe
        });
      });

      scope.savePlayer();
      expect(Coupling.savePlayer).toHaveBeenCalled();
      var callArgs = Coupling.savePlayer.calls.argsFor(0);
      expect(callArgs[0]).toBe(scope.player);

      var savePlayerCallback = callArgs[1];

      expect(location.path).not.toHaveBeenCalled();
      var updatedPlayer = {
        _id: 'newPlayerId'
      };
      savePlayerCallback(updatedPlayer);
      expect(location.path).toHaveBeenCalledWith("/" + routeParams.tribeId + "/player/" + updatedPlayer._id);
    });

  });

  xdescribe('EditPlayerController', function () {
    var ControllerName = 'EditPlayerController';
    var Coupling, location, routeParams;

    beforeEach(function () {
      location = {
        path: jasmine.createSpy('path')
      };
      var selectedTribe = {
        name: 'Party tribe.',
        _id: 'party'
      };
      Coupling = {
        data: {
          selectedTribe: selectedTribe
        },
        selectTribe: jasmine.createSpy('selectTribe'),
        spin: jasmine.createSpy('spin'),
        savePlayer: jasmine.createSpy('save'),
        findPlayerById: jasmine.createSpy('findPlayer'),
        removePlayer: jasmine.createSpy('remove')
      };
      scope.data = Coupling.data;
      scope.$on = jasmine.createSpy('on');
      routeParams = {
        tribeId: selectedTribe._id,
        id: 'thePlayerId'
      };
    });

    it('will select tribe', function () {
      expect(Coupling.selectTribe).not.toHaveBeenCalled();
      injectController(ControllerName, scope, location, Coupling, routeParams);
      expect(Coupling.selectTribe).toHaveBeenCalled();
    });

    it('will maximize player roster', function () {
      scope.playerRoster.minimized = true;
      injectController(ControllerName, scope, location, Coupling, routeParams);
      expect(scope.playerRoster.minimized).toBe(false);
    });

    it('will find the player with given id and provide a duplicate for editing', function () {
      scope.player = null;
      injectController(ControllerName, scope, location, Coupling, routeParams);
      expect(Coupling.findPlayerById).toHaveBeenCalled();
      var argsForCall = Coupling.findPlayerById.calls.argsFor(0);
      expect(argsForCall[0]).toBe(routeParams.id);

      var callback = argsForCall[1];
      var player = {
        name: 'Bobby'
      };
      callback(player);
      expect(scope.original).toBe(player);
      expect(scope.player).not.toBe(player);
      expect(scope.player).toEqual(player);
    });

    it('can save player using Coupling service and redirects to player page on callback', function () {
      injectController(ControllerName, scope, location, Coupling, routeParams);
      scope.playerForm = {
        $setPristine: jasmine.createSpy('pristine')
      };
      scope.savePlayer();
      expect(Coupling.savePlayer).toHaveBeenCalledWith(scope.player);
      expect(scope.playerForm.$setPristine).toHaveBeenCalled();
    });

    it('remove player will remove and reroute to current pair assignments when confirmed', function () {
      spyOn(window, 'confirm');

      injectController(ControllerName, scope, location, Coupling, routeParams);

      window.confirm.and.returnValue(true);
      scope.removePlayer();
      expect(Coupling.removePlayer).toHaveBeenCalled();
      var argsFor = Coupling.removePlayer.calls.argsFor(0);
      expect(argsFor[0]).toBe(scope.player);

      var callback = argsFor[1];
      expect(location.path).not.toHaveBeenCalledWith('/' + routeParams.tribeId + '/pairAssignments/current');
      callback();
      expect(location.path).toHaveBeenCalledWith('/' + routeParams.tribeId + '/pairAssignments/current');
    });

    it('remove player will do nothing when not confirmed', function () {
      window.confirm = jasmine.createSpy('confirm');

      injectController(ControllerName, scope, location, Coupling, routeParams);

      window.confirm.and.returnValue(false);
      scope.removePlayer();
      expect(Coupling.removePlayer).not.toHaveBeenCalled();
      expect(location.path).not.toHaveBeenCalledWith('/' + routeParams.tribeId + '/pairAssignments/current');
    });

    describe('on location change', function () {
      var onLocationChange;
      beforeEach(function () {
        injectController(ControllerName, scope, location, Coupling, routeParams);

        expect(scope.$on).toHaveBeenCalled();
        var args = scope.$on.calls.argsFor(0);
        expect(args[0]).toBe('$locationChangeStart');
        onLocationChange = args[1];
      });

      describe('it will prompt the user to save if the player has changed', function () {

        beforeEach(function () {
          window.confirm = jasmine.createSpy('confirm');
          scope.playerForm = {
            $dirty: true
          };
          scope.original = {
            name: 'O.G.'
          };
          scope.player = {
            name: "differentName"
          };
        });

        it('and if they confirm it will save', function () {
          window.confirm.and.returnValue(true);
          onLocationChange();
          expect(Coupling.savePlayer).toHaveBeenCalledWith(scope.player);
        });

        it('and if they do not confirm it will not save', function () {
          window.confirm.and.returnValue(false);
          onLocationChange();
          expect(Coupling.savePlayer).not.toHaveBeenCalledWith(scope.player);
        });

      });
      it('it will not prompt the user to save if the player is unchanged', function () {
        window.confirm = jasmine.createSpy('confirm');
        scope.playerForm = {
          $dirty: false
        };
        scope.original = {
          name: 'O.G.'
        };
        scope.player = {
          name: scope.original.name
        };
        onLocationChange();
        expect(window.confirm).not.toHaveBeenCalled();
        expect(Coupling.savePlayer).not.toHaveBeenCalled();
      });
    });
  });

  xdescribe('PinListController', function () {

    var routeParams;

    function runPinListController() {
      inject(function ($controller) {
        $controller('PinListController', {
          $scope: scope,
          Coupling: Coupling,
          $routeParams: routeParams
        });
      });
    }

    it('puts the tribe\'s pins on the scope', function (done) {
      scope = {};
      Coupling = {
        promisePins: jasmine.createSpy()
      };
      routeParams = {
        tribeId: 'Somsosomsa'
      };
      var pins = [{
        name: 'pin1'
      }, {
        name: 'pin2'
      }, {
        name: 'pin2'
      }];
      var promise = new RSVP.Promise(function (resolve) {
        resolve(pins);
      });

      Coupling.promisePins.and.returnValue(promise);
      runPinListController();

      promise.then(function () {
        expect(Coupling.promisePins).toHaveBeenCalledWith(routeParams.tribeId);
        expect(scope.pins).toEqual(pins);
        done();

      }).catch(done);
    });
  });
});