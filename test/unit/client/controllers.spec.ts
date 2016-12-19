///<reference path="../../../node_modules/@types/jasmine/index.d.ts"/>
"use strict";

import "angular";
import "angular-mocks";
import * as Promise from "bluebird";
import PlayerCardController from "../../../client/app/components/player-card/PlayerCard";
import {TribeCardController} from "../../../client/app/components/tribe-card/tribe-card";
import {TribeConfigController} from "../../../client/app/components/tribe-config/tribe-config";
import {PairAssignmentsController} from "../../../client/app/components/pair-assignments/pair-assignments";
import {PlayerConfigController} from "../../../client/app/components/player-config/player-config";
import {HistoryController} from "../../../client/app/components/history/history";

const defer = function () {
    const defer = {
        promise: null,
        resolve: null,
        reject: null
    };
    defer.promise = new Promise((resolve, reject) => {
        defer.resolve = resolve;
        defer.reject = reject;
    });
    return defer;
};

describe('The controller named ', function () {

    let scope, Coupling;

    beforeEach(function () {
        scope = {};
        Coupling = {};
    });

    describe('PlayerCardController', function () {

        let controller;
        const location = {
            path: jasmine.createSpy('path')
        };

        beforeEach(function () {
            try {
                controller = new PlayerCardController(location);

                controller.player = {
                    name: 'Chad',
                    _id: 'PrettyGreatPlayerId',
                    tribe: 'awful'
                };

            } catch (err) {
                console.error(err);
                throw err;
            }
        });

        describe('clickPlayerName', function () {

            it('will redirect to the players page', function () {
                const expectedPath = '/' + controller.player.tribe + '/player/' + controller.player._id;
                expect(location.path).not.toHaveBeenCalledWith(expectedPath);
                const event = {};
                controller.clickPlayerName(event);
                expect(location.path).toHaveBeenCalledWith(expectedPath);
            });

            it('will stop propagation to other click events', function () {
                const event = {
                    stopPropagation: jasmine.createSpy('stopPropagation')
                };
                controller.clickPlayerName(event);
                expect(event.stopPropagation).toHaveBeenCalled();
            });
        });

    });

    describe('TribeCardController', function () {

        let location;
        let controller;

        beforeEach(function () {
            location = {
                path: jasmine.createSpy('path')
            };
            controller = new TribeCardController(location);
        });

        describe('clickOnTribeCard', function () {
            it('that changes location to that tribe\'s current pair assignments', function () {
                controller.tribe = {
                    id: 'amazingMagicId'
                };

                expect(location.path).not.toHaveBeenCalled();
                controller.clickOnTribeCard();
                expect(location.path).toHaveBeenCalledWith("/" + controller.tribe.id + "/pairAssignments/current");
            });
        });

        describe('clickOnTribeName', function () {
            it('that changes location to that tribe', function () {
                controller.tribe = {
                    id: 'amazingMagicId'
                };
                expect(location.path).not.toHaveBeenCalled();
                controller.clickOnTribeName({});
                expect(location.path).toHaveBeenCalledWith("/" + controller.tribe.id + '/edit/');
            });

            it('will stop propagation to other click events', function () {
                const event = {
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

    describe('TribeConfigController', function () {

        let Coupling, location, routeParams;
        const selectTribeDefer = defer();
        let selectedTribeId;
        beforeEach(function () {
            location = {
                path: jasmine.createSpy('path')
            };
            const selectedTribe = {
                name: 'Party tribe.',
                id: 'TotallyAwesome',
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
                tribeId: selectedTribe.id
            };
        });

        describe('when pressing the save button ', function () {

            const saveTribeDefer = defer();
            let tribe;
            let controller;

            beforeEach(function () {
                tribe = {
                    $save: jasmine.createSpy('save tribe spy').and.returnValue(saveTribeDefer.promise)
                };
                controller = new TribeConfigController(location);
                controller.tribe = tribe;
            });

            it('will use the Coupling service to save the tribe', function () {
                controller.clickSaveButton();
                expect(tribe.$save).toHaveBeenCalled();
            });

            describe('when the save is complete', function () {
                let callback;
                beforeEach(function () {
                    controller.clickSaveButton();
                    callback = tribe.$save.calls.argsFor(0)[1];
                });

                it('will change the location to the current pair assignments', function (done) {
                    const newTribeId = 'expectedId';
                    const expectedPath = '/tribes';
                    expect(location.path).not.toHaveBeenCalledWith(expectedPath);

                    const updatedTribe = {
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
        let Coupling, location, routeParams;
        const spinDefer = defer();
        const selectedTribe = {
            name: 'Party tribe.',
            _id: 'party',
            id: 'hmm'
        };

        const tribe = selectedTribe;

        const players = [{
            _id: 'h8',
            tribe: '1'
        }, {
            _id: '3r',
            tribe: '1'
        }, {
            _id: '8d3',
            tribe: '1'
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
                tribeId: selectedTribe.id
            };
        });

        it('save will use Coupling service to save and then will redirect to the current pair assignments page', function (done) {
            const controller = new PairAssignmentsController(Coupling, location);
            controller.tribe = tribe;
            controller.players = players;

            expect(Coupling.saveCurrentPairAssignments).not.toHaveBeenCalled();

            const successPromise = Promise.resolve('Complete');
            Coupling.saveCurrentPairAssignments.and.returnValue(successPromise);

            controller.save();
            expect(Coupling.saveCurrentPairAssignments).toHaveBeenCalled();
            successPromise.then(function () {
                expect(location.path).toHaveBeenCalledWith("/" + routeParams.tribeId + "/pairAssignments/current");
                done();
            });
        });

        it('onDrop will take two players and swap their places', function () {
            const controller = new PairAssignmentsController(Coupling, location);

            const player1 = {
                _id: '1',
                name: '1',
                tribe: 'numbers'
            };

            const player2 = {
                _id: '2',
                name: '2',
                tribe: 'numbers'
            };
            const player3 = {
                _id: '3',
                name: '3',
                tribe: 'numbers'
            };
            const player4 = {
                _id: '4',
                name: '4',
                tribe: 'numbers'
            };

            controller.pairAssignments = {
                pairs: [
                    [player1, player2],
                    [player3, player4]
                ],
                date: '',
                tribe: 'numbers'
            };

            controller.onDrop(player2, player3);
            expect(controller.pairAssignments.pairs).toEqual([
                [player1, player3],
                [player2, player4]
            ]);
        });

        it('onDrop will not swap players that are already paired', function () {
            const controller = new PairAssignmentsController(Coupling, location);
            const player1 = {
                _id: '1',
                name: '1',
                tribe: 'numbers'
            };
            const player2 = {
                _id: '2',
                name: '2',
                tribe: 'numbers'
            };
            const player3 = {
                _id: '3',
                name: '3',
                tribe: 'numbers'
            };
            const player4 = {
                _id: '4',
                name: '4',
                tribe: 'numbers'
            };

            controller.pairAssignments = {
                pairs: [
                    [player1, player2],
                    [player3, player4]
                ],
                date: '',
                tribe: 'numbers'
            };

            controller.onDrop(player4, player3);
            expect(controller.pairAssignments.pairs).toEqual([
                [player1, player2],
                [player3, player4]
            ]);
        });
    });

    describe('PairAssignmentsController', function () {
        let Coupling, location, routeParams;

        const selectTribeDefer = defer();
        let selectedTribeId;

        const selectedTribe = {
            name: 'Party tribe.',
            id: 'party'
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
                tribeId: selectedTribe.id
            };
        });

        it('will provide all of the players that are not in the current pairs', function () {
            const tribeId = 'numbers';
            const currentPairs = [
                [
                    {name: 'tom', _id: '0', tribe: tribeId},
                    {name: 'jerry', _id: 'z', tribe: tribeId}
                ], [
                    {name: 'fellow', _id: '3', tribe: tribeId},
                    {name: 'guy', _id: '2', tribe: tribeId}
                ]
            ];
            const players = [
                {name: 'rigby', _id: '1', tribe: tribeId},
                {name: 'guy', _id: '2', tribe: tribeId},
                {name: 'fellow', _id: '3', tribe: tribeId},
                {name: 'nerd', _id: '4', tribe: tribeId},
                {name: 'pantsmaster', _id: '5', tribe: tribeId}];
            const controller = new PairAssignmentsController(Coupling, location);
            controller.pairAssignments = {pairs: currentPairs, tribe: tribeId, date: ''};
            controller.players = players;

            expect(controller.unpairedPlayers).toEqual([
                {name: 'rigby', _id: '1', tribe: tribeId},
                {name: 'nerd', _id: '4', tribe: tribeId},
                {name: 'pantsmaster', _id: '5', tribe: tribeId}
            ]);
        });

        it('will put no pair assignments on scope when there is no history', function () {
            const tribeId = 'numbers';
            const players = [
                {name: 'rigby', _id: '1', tribe: tribeId},
                {name: 'guy', _id: '2', tribe: tribeId},
                {name: 'fellow', _id: '3', tribe: tribeId},
                {name: 'nerd', _id: '4', tribe: tribeId},
                {name: 'pantsmaster', _id: '5', tribe: tribeId}];
            const controller = new PairAssignmentsController(Coupling, location);
            controller.pairAssignments = undefined;
            controller.players = players;
            expect(controller.unpairedPlayers).toEqual(players);
        })
    });

    describe('PlayerConfigController', function () {

        const tribe = {
            name: 'Party tribe.',
            id: 'party',
            _id: 'hmm'
        };

        const player = {_id: 'blarg', tribe: tribe.id};

        let Coupling, location;

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

        let controller;
        it('can save player using Coupling service and then reloads', function (done) {
            const $route = {
                current: {params: {id: player._id}, locals: null},
                routes: '',
                reload: jasmine.createSpy('path')
            };

            inject(function ($q, $rootScope) {
                const saveDefer = defer();
                Coupling.savePlayer.and.returnValue(saveDefer.promise);
                controller = new PlayerConfigController(scope, Coupling, location, $route);

                controller.player = player;
                controller.tribe = tribe;

                controller.player.name = 'nonsense';
                controller.savePlayer();
                expect(Coupling.savePlayer).toHaveBeenCalledWith(controller.player);
                saveDefer.resolve();


                saveDefer.promise
                    .then(function () {
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
                const confirmSpy = spyOn(window, 'confirm');

                const deleteDefer = $q.defer();
                Coupling.removePlayer.and.returnValue(deleteDefer.promise);


                const route = {
                    current: {params: {id: player._id}, locals: null},
                    routes: '',
                    reload: jasmine.createSpy('path')
                };
                const controller = new PlayerConfigController(scope, Coupling, location, route);

                controller.player = player;
                controller.tribe = tribe;

                confirmSpy.and.returnValue(true);
                controller.removePlayer();
                expect(Coupling.removePlayer).toHaveBeenCalled();
                const argsFor = Coupling.removePlayer.calls.argsFor(0);
                expect(argsFor[0]).toBe(controller.player);

                expect(location.path).not.toHaveBeenCalledWith('/' + tribe.id + '/pairAssignments/current');
                deleteDefer.resolve();
                $rootScope.$apply();
                expect(location.path).toHaveBeenCalledWith('/' + tribe.id + '/pairAssignments/current');
            }));

        it('remove player will do nothing when not confirmed', function () {
            const confirmSpy = jasmine.createSpy('confirm');
            window.confirm = confirmSpy;

            const route = {
                current: {params: {id: player._id}, locals: null},
                routes: '',
                reload: jasmine.createSpy('path')
            };
            controller = new PlayerConfigController(scope, Coupling, location, route);

            confirmSpy.and.returnValue(false);
            controller.removePlayer();
            expect(Coupling.removePlayer).not.toHaveBeenCalled();
            expect(location.path).not.toHaveBeenCalledWith('/' + tribe.id + '/pairAssignments/current');
        });

        describe('on location change', function () {
            let onLocationChange;

            const player = {_id: 'blarg'};
            let controller;

            beforeEach(function () {
                const route = {
                    current: {params: {id: player._id}, locals: null},
                    routes: '',
                    reload: jasmine.createSpy('path')
                };
                controller = new PlayerConfigController(scope, Coupling, location, route);
                expect(scope.$on).toHaveBeenCalled();
                const args = scope.$on.calls.argsFor(0);
                expect(args[0]).toBe('$locationChangeStart');
                onLocationChange = args[1];
            });

            describe('it will prompt the user to save if the player has changed', function () {

                beforeEach(function () {
                    this.confirmSpy = jasmine.createSpy('confirm');
                    window.confirm = this.confirmSpy;

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
                    this.confirmSpy.and.returnValue(true);
                    onLocationChange();
                    expect(Coupling.savePlayer).toHaveBeenCalledWith(controller.player);
                });

                it('and if they do not confirm it will not save', function () {
                    this.confirmSpy.and.returnValue(false);
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

    describe('HistoryController', function () {
        it('will delete pair set when remove is called and confirmed', function () {
            const entry = {
                $remove: jasmine.createSpy('removeSpy')
            };
            const historyController = new HistoryController();
            window.confirm = jasmine.createSpy('confirm').and.returnValue(true);

            historyController.removeEntry(entry);
            expect(entry.$remove).toHaveBeenCalled();
        });

        it('will not delete pair set when remove is called and not confirmed', function () {
            const entry = {
                $remove: jasmine.createSpy('removeSpy')
            };
            const historyController = new HistoryController();

            window.confirm = jasmine.createSpy('confirm').and.returnValue(false);

            historyController.removeEntry(entry);
            expect(entry.$remove).not.toHaveBeenCalled();
        });
    });

});