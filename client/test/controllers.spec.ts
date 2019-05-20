"use strict";

import "angular";
import "angular-mocks";
import {TribeCardController} from "../app/components/tribe-card/TribeCardController";
import {PairAssignmentsController} from "../app/components/pair-assignments/PairAssignmentsDirective";
import {HistoryController} from "../app/components/history/history";
import Pair from "../../common/Pair";
import {PlayerCardController} from "../app/components/player-card/player-card";
import PairAssignmentSet from "../../common/PairAssignmentSet";

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

    let scope;

    beforeEach(function () {
        scope = {};
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
            const controller = new PairAssignmentsController(Coupling, location, {});
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
            const controller = new PairAssignmentsController(Coupling, location, {});

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
                date: ''
            };

            controller.onDrop(player2, player3);
            expect(controller.pairAssignments.pairs).toEqual([
                [player1, player3],
                [player2, player4]
            ]);
        });

        it('onDrop will not swap players that are already paired', function () {
            const controller = new PairAssignmentsController(Coupling, location, {});
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
                date: ''
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
            const currentPairs: Pair[] = [
                [
                    {name: 'tom', _id: '0'},
                    {name: 'jerry', _id: 'z'}
                ], [
                    {name: 'fellow', _id: '3'},
                    {name: 'guy', _id: '2'}
                ]
            ];
            const players = [
                {name: 'rigby', _id: '1'},
                {name: 'guy', _id: '2'},
                {name: 'fellow', _id: '3'},
                {name: 'nerd', _id: '4'},
                {name: 'pantsmaster', _id: '5'}];
            const controller = new PairAssignmentsController(Coupling, location, {});
            controller.pairAssignments = {pairs: currentPairs, date: ''};
            controller.players = players;

            expect(controller.unpairedPlayers).toEqual([
                {name: 'rigby', _id: '1'},
                {name: 'nerd', _id: '4'},
                {name: 'pantsmaster', _id: '5'}
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
            const controller = new PairAssignmentsController(Coupling, location, {});
            controller.pairAssignments = undefined;
            controller.players = players;
            expect(controller.unpairedPlayers).toEqual(players);
        })
    });

    describe('HistoryController', function () {
        it('will delete pair set when remove is called and confirmed', async function () {
            const $remove = jasmine.createSpy('removeSpy');
            const reload = jasmine.createSpy('reload');
            const coupling = {
                removeAssignments: $remove
            };
            const historyController = new HistoryController(coupling, {reload: reload});
            historyController.tribe = {id: 'me', name: 'you'};
            spyOn(window, 'confirm').and.returnValue(true);

            const entry: PairAssignmentSet = {
                pairs: [],
                date: ''
            };
            await historyController.removeEntry(entry);
            expect($remove).toHaveBeenCalled();
            expect(reload).toHaveBeenCalled();
        });

        it('will not delete pair set when remove is called and not confirmed', async function () {
            const $remove = jasmine.createSpy('removeSpy');
            const coupling = {
                removeAssignments: $remove
            };
            const historyController = new HistoryController(coupling, {});

            spyOn(window, 'confirm').and.returnValue(false);

            const entry: PairAssignmentSet = {
                pairs: [],
                date: '',
            };
            await historyController.removeEntry(entry);
            expect($remove).not.toHaveBeenCalled();
        });
    });

});