"use strict";

describe('The controller named ', function () {

  var scope, Coupling;

  beforeEach(function () {
    module("coupling.controllers");
    scope = {};
    Coupling = {};
  });

  describe('PlayerCardController', function () {
    var controller;
    var location = {
      path: jasmine.createSpy('path')
    };

    beforeEach(function () {
      inject(function ($controller) {
        controller = $controller('PlayerCardController', {
          $scope: scope,
          $location: location,
          Coupling: Coupling
        })
      });
      controller.player = {
        name: 'Chad',
        _id: 'PrettyGreatPlayerId',
        tribe: 'awful'
      };
    });

    describe('clickPlayerName', function () {
      it('will redirect to the players page', function () {
        var expectedPath = '/' + controller.player.tribe + '/player/' + controller.player._id;
        expect(location.path).not.toHaveBeenCalledWith(expectedPath);
        var event = {};
        controller.clickPlayerName(controller.player._id, event);
        expect(location.path).toHaveBeenCalledWith(expectedPath);
      });

      it('will stop propagation to other click events', function () {
        var event = {
          stopPropagation: jasmine.createSpy('stopPropagation')
        };
        controller.clickPlayerName(event);
        expect(event.stopPropagation).toHaveBeenCalled();
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

    var location;
    var controller;

    beforeEach(function () {
      location = {
        path: jasmine.createSpy('path')
      };
      inject(function ($controller) {
        controller = $controller('TribeCardController', {
          $location: location
        });
      });
    });

    describe('clickOnTribeCard', function () {
      it('that changes location to that tribe\'s current pair assignments', function () {
        controller.tribe = {
          _id: 'amazingMagicId'
        };

        expect(location.path).not.toHaveBeenCalled();
        controller.clickOnTribeCard();
        expect(location.path).toHaveBeenCalledWith("/" + controller.tribe._id + "/pairAssignments/current");
      });
    });

    describe('clickOnTribeName', function () {
      it('that changes location to that tribe', function () {
        controller.tribe = {
          _id: 'amazingMagicId'
        };
        expect(location.path).not.toHaveBeenCalled();
        controller.clickOnTribeName({});
        expect(location.path).toHaveBeenCalledWith("/" + controller.tribe._id + '/edit/');
      });

      it('will stop propagation to other click events', function () {
        var event = {
          stopPropagation: jasmine.createSpy('stopPropagation')
        };
        controller.tribe = {
          _id: 'amazingMagicId'
        };
        controller.clickOnTribeName(event);
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
      var tribe = {};
      var controller;

      beforeEach(function () {
        tribe.$save = jasmine.createSpy('save tribe spy').and.returnValue(saveTribeDefer.promise);

        inject(function ($controller) {
          controller = $controller(TribeConfigController, {
            $location: location,
            Coupling: Coupling
          });
          controller.tribe = tribe;
        });
      });

      it('will use the Coupling service to save the tribe', function () {
        controller.clickSaveButton();
        expect(tribe.$save).toHaveBeenCalled();
      });

      describe('when the save is complete', function () {
        var callback;
        beforeEach(function () {
          controller.clickSaveButton();
          callback = tribe.$save.calls.argsFor(0)[1];
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

  describe('PairAssignmentsController', function () {
    var ControllerName = 'PairAssignmentsController';
    var Coupling, location, routeParams;
    var spinDefer = new RSVP.defer();
    var selectedTribe = {
      name: 'Party tribe.',
      _id: 'party'
    };

    var tribe = selectedTribe;

    var players = [{
      _id: 'h8'
    }, {
      _id: '3r'
    }, {
      _id: '8d3'
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

    it('save will use Coupling service to save and then will redirect to the current pair assignments page', function (done) {
      inject(function ($controller) {
        var controller = $controller(ControllerName, {
          $location: location,
          Coupling: Coupling
        });
        controller.tribe = tribe;
        controller.players = players;

        expect(Coupling.saveCurrentPairAssignments).not.toHaveBeenCalled();

        var successPromise = RSVP.resolve('Complete');
        Coupling.saveCurrentPairAssignments.and.returnValue(successPromise);

        controller.save();
        expect(Coupling.saveCurrentPairAssignments).toHaveBeenCalled();
        successPromise.then(function () {
          expect(location.path).toHaveBeenCalledWith("/" + routeParams.tribeId + "/pairAssignments/current");
          done();
        });
      });
    });

    it('onDrop will take two players and swap their places', function () {
      inject(function ($controller) {
        var controller = $controller(ControllerName, {
          $scope: scope,
          $location: location,
          Coupling: Coupling,
          $routeParams: routeParams,
          requirements: {
            tribe: tribe,
            players: players
          }
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

        controller.pairAssignments = {
          pairs: [
            [player1, player2],
            [player3, player4]
          ]
        };

        controller.onDrop(player2, player3);
        expect(controller.pairAssignments.pairs).toEqual([
          [player1, player3],
          [player2, player4]
        ]);
      });
    });

    it('onDrop will not swap players that are already paired', function () {
      inject(function ($controller) {
        var controller = $controller(ControllerName, {
          $scope: scope,
          $location: location,
          Coupling: Coupling,
          $routeParams: routeParams,
          requirements: {
            tribe: tribe,
            players: players
          }
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

        controller.pairAssignments = {
          pairs: [
            [player1, player2],
            [player3, player4]
          ]
        };

        controller.onDrop(player4, player3);
        expect(controller.pairAssignments.pairs).toEqual([
          [player1, player2],
          [player3, player4]
        ]);
      });
    });
  });

  describe('PairAssignmentsController', function () {
    var ControllerName = 'PairAssignmentsController';
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

    it('will provide all of the players that are not in the current pairs', function () {
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
        var controller = $controller(ControllerName, {});
        controller.pairAssignments = {pairs: currentPairs};
        controller.players = players;

        expect(controller.unpairedPlayers).toEqual([
          {name: 'rigby', _id: '1'},
          {name: 'nerd', _id: '4'},
          {name: 'pantsmaster', _id: '5'}
        ]);
      });
    });

    it('will put no pair assignments on scope when there is no history', function () {
      var players = [
        {name: 'rigby', _id: '1'},
        {name: 'guy', _id: '2'},
        {name: 'fellow', _id: '3'},
        {name: 'nerd', _id: '4'},
        {name: 'pantsmaster', _id: '5'}];
      inject(function ($controller) {
        var controller = $controller(ControllerName, {});
        controller.pairAssignments = undefined;
        controller.players = players;
        expect(controller.unpairedPlayers).toEqual(players);
      });
    })
  });

  describe('PlayerConfigController', function () {
    var ControllerName = 'PlayerConfigController';

    var tribe = {
      name: 'Party tribe.',
      _id: 'party'
    };

    var player = {_id: 'blarg', tribe: tribe._id};

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

    var controller;
    it('can save player using Coupling service and then reloads', function (done) {
      var $route = {
        current: {params: {id: player._id}},
        reload: jasmine.createSpy('path')
      };
      inject(function ($controller, $q, $rootScope) {
        var saveDefer = $q.defer();
        Coupling.savePlayer.and.returnValue(saveDefer.promise);
        controller = $controller(ControllerName, {
          $scope: scope,
          Coupling: Coupling,
          $location: location,
          $route: $route,
          tribe: tribe,
          players: [player]
        });

        controller.player = player;
        controller.tribe = tribe;

        controller.player.name = 'nonsense';
        controller.savePlayer();
        expect(Coupling.savePlayer).toHaveBeenCalledWith(controller.player);
        saveDefer.resolve();


        saveDefer.promise
          .then(function () {
            console.log('lol');
            expect($route.reload).toHaveBeenCalled();
            done();
          })
          .catch(function (err) {
            done.fail(err);
          });
        $rootScope.$apply();
      });
    });

    it('remove player will remove and reroute to current pair assignments when confirmed',
      inject(function ($controller, $q, $rootScope) {
        spyOn(window, 'confirm');

        var deleteDefer = $q.defer();
        Coupling.removePlayer.and.returnValue(deleteDefer.promise);

        var controller = $controller(ControllerName, {
          $scope: scope,
          Coupling: Coupling,
          $location: location,
          $route: {current: {params: {id: player._id}}}
        });

        controller.player = player;
        controller.tribe = tribe;

        window.confirm.and.returnValue(true);
        controller.removePlayer();
        expect(Coupling.removePlayer).toHaveBeenCalled();
        var argsFor = Coupling.removePlayer.calls.argsFor(0);
        expect(argsFor[0]).toBe(controller.player);

        expect(location.path).not.toHaveBeenCalledWith('/' + tribe._id + '/pairAssignments/current');
        deleteDefer.resolve();
        $rootScope.$apply();
        expect(location.path).toHaveBeenCalledWith('/' + tribe._id + '/pairAssignments/current');
      }));

    it('remove player will do nothing when not confirmed', function () {
      window.confirm = jasmine.createSpy('confirm');

      inject(function ($controller) {
        controller = $controller(ControllerName, {
          $scope: scope,
          Coupling: Coupling,
          $location: location,
          $route: {current: {params: {id: player._id}}},
          tribe: tribe,
          players: [player]
        });
      });

      window.confirm.and.returnValue(false);
      controller.removePlayer();
      expect(Coupling.removePlayer).not.toHaveBeenCalled();
      expect(location.path).not.toHaveBeenCalledWith('/' + tribe._id + '/pairAssignments/current');
    });

    describe('on location change', function () {
      var onLocationChange;

      var tribe = {_id: 'lol'};
      var player = {_id: 'blarg'};
      var controller;

      beforeEach(function () {
        inject(function ($controller) {
          controller = $controller(ControllerName, {
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
          expect(Coupling.savePlayer).toHaveBeenCalledWith(controller.player);
        });

        it('and if they do not confirm it will not save', function () {
          window.confirm.and.returnValue(false);
          onLocationChange();
          expect(Coupling.savePlayer).not.toHaveBeenCalledWith(controller.player);
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
});