"use strict";

describe('The controller named ', function () {

  var scope, Coupling;

  beforeEach(function () {
    module("coupling.controllers");
    scope = {};
    Coupling = {};
  });

  describe('SelectedPlayerCardController', function () {

    var location = {
      path: jasmine.createSpy('path')
    };

    beforeEach(function () {
      scope.player = {
        name: 'Chad',
        _id: 'PrettyGreatPlayerId',
        tribe: 'awful',
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
        var expectedPath = '/' + scope.player.tribe + '/player/' + scope.player._id;
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

  describe('TribeCardController', function () {

    var Coupling, location;
    beforeEach(function () {
      location = {
        path: jasmine.createSpy('path')
      };
      Coupling = {
        data: {},
        selectTribe: jasmine.createSpy('selectTribe')
      };
      inject(function ($controller) {
        $controller('TribeCardController', {
          $scope: scope,
          $location: location
        });
      });
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
        scope.clickOnTribeName(tribe, {});
        expect(location.path).toHaveBeenCalledWith("/" + tribe._id + '/edit/');
      });

      it('will stop propagation to other click events', function () {
        var event = {
          stopPropagation: jasmine.createSpy('stopPropagation')
        };
        var tribe = {
          _id: 'amazingMagicId'
        };
        scope.clickOnTribeName(tribe, event);
        expect(event.stopPropagation).toHaveBeenCalled();
      });
    });
  });

  var TribeConfigController = 'TribeConfigController';
  describe(TribeConfigController, function () {

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

    describe('when pressing the save button ', function () {

      var saveTribeDefer = new RSVP.defer();
      beforeEach(function () {
        Coupling.saveTribe = jasmine.createSpy('save tribe spy')
          .and.returnValue(saveTribeDefer.promise);

        inject(function ($controller) {
          $controller(TribeConfigController, {
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

        it('will change the location to the current pair assignments', function (done) {
          var newTribeId = 'expectedId';
          var expectedPath = '/tribes';
          expect(location.path).not.toHaveBeenCalledWith(expectedPath);

          var updatedTribe = {
            _id: newTribeId
          };
          saveTribeDefer.resolve(updatedTribe);
          saveTribeDefer.promise.then(function () {
            expect(location.path).toHaveBeenCalledWith(expectedPath);
            done();
          })
        });
      });
    });
  });

  describe('NewPairAssignmentsController', function () {
    var ControllerName = 'NewPairAssignmentsController';
    var Coupling, location, routeParams;
    var spinDefer = new RSVP.defer();
    var selectedTribe = {
      name: 'Party tribe.',
      _id: 'party'
    };

    var tribe = selectedTribe;

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


    beforeEach(function () {
      location = {
        path: jasmine.createSpy('path')
      };

      Coupling = {
        data: {},
        spin: jasmine.createSpy('spin'),
        saveCurrentPairAssignments: jasmine.createSpy('save')
      };
      Coupling.spin.and.returnValue(spinDefer.promise);
      scope.data = Coupling.data;
      routeParams = {
        tribeId: selectedTribe._id
      };
    });

    it('will select tribe and spin all selected players', function (done) {
      inject(function ($controller) {
        $controller(ControllerName, {
          $scope: scope,
          $location: location,
          Coupling: Coupling,
          $routeParams: routeParams,
          tribe: tribe,
          players: players
        });
      });
      expect(scope.tribe).toBe(tribe);
      expect(Coupling.spin).toHaveBeenCalledWith([players[1], players[2]], scope.tribe._id);
      var pairs = [['lol', 'olol']];
      spinDefer.resolve(pairs);
      spinDefer.promise.then(function () {
        expect(scope.currentPairAssignments).toBe(pairs);
        done();
      })
    });

    it('save will use Coupling service to save and then will redirect to the current pair assignments page', function (done) {
      inject(function ($controller) {
        $controller(ControllerName, {
          $scope: scope,
          $location: location,
          Coupling: Coupling,
          $routeParams: routeParams,
          tribe: tribe,
          players: players
        });
      });
      expect(Coupling.saveCurrentPairAssignments).not.toHaveBeenCalled();

      var successPromise = RSVP.resolve('Complete');
      Coupling.saveCurrentPairAssignments.and.returnValue(successPromise);

      scope.save();
      expect(Coupling.saveCurrentPairAssignments).toHaveBeenCalled();
      successPromise.then(function () {
        expect(location.path).toHaveBeenCalledWith("/" + routeParams.tribeId + "/pairAssignments/current");
        done();
      });
    });

    it('onDrop will take two players and swap their places', function () {
      inject(function ($controller) {
        $controller(ControllerName, {
          $scope: scope,
          $location: location,
          Coupling: Coupling,
          $routeParams: routeParams,
          tribe: tribe,
          players: players
        });
      });
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

      scope.currentPairAssignments = {
        pairs: [
          [player1, player2],
          [player3, player4]
        ]
      };

      scope.onDrop(null, player2, player3);
      expect(scope.currentPairAssignments.pairs).toEqual([
        [player1, player3],
        [player2, player4]
      ]);
    });

    it('onDrop will not swap players that are already paired', function () {
      inject(function ($controller) {
        $controller(ControllerName, {
          $scope: scope,
          $location: location,
          Coupling: Coupling,
          $routeParams: routeParams,
          tribe: tribe,
          players: players
        });
      });
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

      scope.currentPairAssignments = {
        pairs: [
          [player1, player2],
          [player3, player4]
        ]
      };

      scope.onDrop(null, player4, player3);
      expect(scope.currentPairAssignments.pairs).toEqual([
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

    it('will put the latest pairs and players on scope', function () {
      var currentPairs = [
        [{name: 'tom'}, {name: 'jerry'}]
      ];
      var players = [{name: 'guy'}, {name: 'fellow'}, {name: 'nerd'}];
      var currentPairsDocument = {pairs: currentPairs};
      inject(function ($controller) {
        $controller(ControllerName, {
          $scope: scope,
          Coupling: Coupling,
          pairAssignmentDocument: currentPairsDocument,
          tribe: selectedTribe,
          players: players
        });
      });
      expect(scope.currentPairAssignments).toBe(currentPairsDocument);
      expect(scope.players).toBe(players);
    });

    it('will put all of the players that are not in the current pairs on the scope', function () {
      var currentPairs = [
        [{name: 'tom', _id: '0'}, {name: 'jerry', _id: 'z'}], [{name: 'fellow', _id: '3'}, {name: 'guy', _id: '2'}]
      ];
      var players = [
        {name: 'rigby', _id: '1'},
        {name: 'guy', _id: '2'},
        {name: 'fellow', _id: '3'},
        {name: 'nerd', _id: '4'},
        {name: 'pantsmaster', _id: '5'}];
      inject(function ($controller) {
        $controller(ControllerName, {
          $scope: scope,
          Coupling: Coupling,
          pairAssignmentDocument: {pairs: currentPairs},
          tribe: selectedTribe,
          players: players
        });
      });
      expect(scope.unpairedPlayers).toEqual([
        {name: 'rigby', _id: '1'},
        {name: 'nerd', _id: '4'},
        {name: 'pantsmaster', _id: '5'}
      ]);
    });

    it('will put no pair assignments on scope when there is no history', function () {
      var players = [
        {name: 'rigby', _id: '1'},
        {name: 'guy', _id: '2'},
        {name: 'fellow', _id: '3'},
        {name: 'nerd', _id: '4'},
        {name: 'pantsmaster', _id: '5'}];
      inject(function ($controller) {
        $controller(ControllerName, {
          $scope: scope,
          Coupling: Coupling,
          pairAssignmentDocument: undefined,
          tribe: selectedTribe,
          players: players
        });
      });
      expect(scope.unpairedPlayers).toEqual(players);
    })
  });

  describe('PrepareController', function () {
    xit('the spin button passes selected players to the next route', function () {
    });
  });

  describe('PlayerConfigController', function () {
    var ControllerName = 'PlayerConfigController';

    var tribe = {
      name: 'Party tribe.',
      _id: 'party'
    };

    var player = {_id: 'blarg'};

    var Coupling, location;

    beforeEach(function () {
      location = {
        path: jasmine.createSpy('path')
      };

      Coupling = {
        spin: jasmine.createSpy('spin'),
        savePlayer: jasmine.createSpy('save'),
        removePlayer: jasmine.createSpy('remove')
      };
      scope.data = Coupling.data;
      scope.$on = jasmine.createSpy('on');
    });

    it('will duplicate player for editing', function () {
      scope.player = player;
      scope.tribe = tribe;
      inject(function ($controller) {
        $controller(ControllerName, {
          $scope: scope,
          $location: location,
          $route: {current: {params: {id: player._id}}}
        });
      });
      expect(scope.original).toBe(player);
      expect(scope.player).not.toBe(player);
      expect(scope.player).toEqual(player);
    });

    it('can save player using Coupling service and then reloads', function () {
      var $route = {
        current: {params: {id: player._id}},
        reload: jasmine.createSpy('path')
      };
      scope.player = player;
      scope.tribe = tribe;
      inject(function ($controller) {
        $controller(ControllerName, {
          $scope: scope,
          Coupling: Coupling,
          $location: location,
          $route: $route,
          tribe: tribe,
          players: [player]
        });
      });
      scope.player.name = 'nonsense';
      scope.savePlayer();
      expect(Coupling.savePlayer).toHaveBeenCalledWith(scope.player);
      expect($route.reload).toHaveBeenCalled();
    });

    it('remove player will remove and reroute to current pair assignments when confirmed',
      inject(function ($controller, $q, $rootScope) {
        spyOn(window, 'confirm');

        var deleteDefer = $q.defer();
        Coupling.removePlayer.and.returnValue(deleteDefer.promise);

        scope.player = player;
        scope.tribe = tribe;
        $controller(ControllerName, {
          $scope: scope,
          Coupling: Coupling,
          $location: location,
          $route: {current: {params: {id: player._id}}}
        });

        window.confirm.and.returnValue(true);
        scope.removePlayer();
        expect(Coupling.removePlayer).toHaveBeenCalled();
        var argsFor = Coupling.removePlayer.calls.argsFor(0);
        expect(argsFor[0]).toBe(scope.player);

        expect(location.path).not.toHaveBeenCalledWith('/' + tribe._id + '/pairAssignments/current');
        deleteDefer.resolve();
        $rootScope.$apply();
        expect(location.path).toHaveBeenCalledWith('/' + tribe._id + '/pairAssignments/current');
      }));

    it('remove player will do nothing when not confirmed', function () {
      window.confirm = jasmine.createSpy('confirm');

      inject(function ($controller) {
        $controller(ControllerName, {
          $scope: scope,
          Coupling: Coupling,
          $location: location,
          $route: {current: {params: {id: player._id}}},
          tribe: tribe,
          players: [player]
        });
      });

      window.confirm.and.returnValue(false);
      scope.removePlayer();
      expect(Coupling.removePlayer).not.toHaveBeenCalled();
      expect(location.path).not.toHaveBeenCalledWith('/' + tribe._id + '/pairAssignments/current');
    });

    describe('on location change', function () {
      var onLocationChange;

      var tribe = {_id: 'lol'};
      var player = {_id: 'blarg'};

      beforeEach(function () {
        inject(function ($controller) {
          $controller(ControllerName, {
            $scope: scope,
            Coupling: Coupling,
            $route: {current: {params: {id: player._id}}},
            $location: location,
            tribe: tribe,
            players: [player]
          });
        });
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

  describe('PinListController', function () {

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