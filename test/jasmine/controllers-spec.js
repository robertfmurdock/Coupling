"use strict";

describe('The controller named ', function () {

    beforeEach(function () {
        module("coupling.controllers");
    });

    describe('CouplingController', function () {

        it('will redirect to the tribes page', function () {
            var scope = {unique: "value"};
            var pathSpy = jasmine.createSpy('path');
            var location = {path: pathSpy};
            var Coupling = {data: {}};

            pathSpy.and.returnValue('/');
            initializeCouplingControllerFactory(scope, location, Coupling);
            expect(pathSpy).toHaveBeenCalledWith('/tribes');
        });

        function initializeCouplingControllerFactory(scope, location, Coupling) {
            inject(function ($controller) {
                $controller('CouplingController', {
                    $scope: scope,
                    $location: location,
                    Coupling: Coupling
                });
            });
        }

        describe('on most pages', function () {

            var scope, location, Coupling;

            beforeEach(function () {
                scope = {unique: "value"};
                location = {path: jasmine.createSpy('path')};
                Coupling = {data: {selectedTribeId: 'AwesomeTribe'}};
                initializeCouplingControllerFactory(scope, location, Coupling)
            });

            it('will not redirect', function () {
                expect(location.path.calls.count()).toEqual(1);
            });

            it('will add data to the scope', function () {
                expect(scope.data).toBe(Coupling.data);
            });


            describe('spin', function () {
                it('will redirect to the new pair assignments page', function () {
                    expect(location.path).not.toHaveBeenCalledWith(Coupling.data.selectedTribeId + '/pairAssignments/new');
                    scope.clickSpinButton();
                    expect(location.path).toHaveBeenCalledWith(Coupling.data.selectedTribeId + '/pairAssignments/new');
                });
            });

            describe('the hide players state', function () {
                it('starts as default', function () {
                    expect(scope.playerRoster.minimized).toBe(false);
                });

                it('clickPlayerRosterHeader will flip the hide players state', function () {
                    scope.clickPlayerRosterHeader();
                    expect(scope.playerRoster.minimized).toBe(true);
                    scope.clickPlayerRosterHeader();
                    expect(scope.playerRoster.minimized).toBe(false);
                    scope.clickPlayerRosterHeader();
                    expect(scope.playerRoster.minimized).toBe(true);
                });

            });

            describe('SelectedPlayerCardController', function () {

                beforeEach(function () {
                    scope.player = {name: 'Chad', _id: 'PrettyGreatPlayerId'};
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
                        var event = {stopPropagation: jasmine.createSpy('stopPropagation')};
                        scope.clickPlayerName(event);
                        expect(event.stopPropagation).toHaveBeenCalled();
                    });
                });

                it('player is initially selected', function () {
                    expect(scope.isDisabled).toBe(false);
                });

                describe('clickPlayerCard', function () {
                    it('will change a players selection in the map', function () {
                        expect(scope.isDisabled).toBe(false);
                        scope.clickPlayerCard();
                        expect(scope.isDisabled).toBe(true);
                        scope.clickPlayerCard();
                        expect(scope.isDisabled).toBe(false);
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
                    location = {path: jasmine.createSpy('path')};
                    Coupling = {data: {}, selectTribe: jasmine.createSpy('selectTribe')};
                });

                it('will deselect tribe', function () {
                    checkControllerWillDeselectTribe('TribeListController', Coupling, location);
                });

                it('will hide players', function () {
                    scope.playerRoster.minimized = false;
                    injectController('TribeListController', scope, location, Coupling);
                    expect(scope.playerRoster.minimized).toBe(true);
                });

                describe('scopes a function named', function () {
                    beforeEach(function () {
                        injectController('TribeListController', scope, location, Coupling);
                    });

                    describe('clickOnTribeCard', function () {
                        it('that changes location to that tribe\'s current pair assignments', function () {
                            var tribe = {_id: 'amazingMagicId'};
                            expect(location.path).not.toHaveBeenCalled();
                            scope.clickOnTribeCard(tribe);
                            expect(location.path).toHaveBeenCalledWith("/" + tribe._id + "/pairAssignments/current");
                        });
                    });

                    describe('clickOnTribeName', function () {
                        it('that changes location to that tribe', function () {
                            var tribe = {_id: 'amazingMagicId'};
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
                    location = {path: jasmine.createSpy('path')};
                    Coupling = {data: {}, selectTribe: jasmine.createSpy('selectTribe')};
                });

                it('creates and selects a new tribe', function () {
                    var previouslySelectedTribe = {name: 'This should not be the tribe after injection.'};
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
                            var expectedPath = '/' + newTribeId + '/pairAssignments/current';
                            expect(location.path).not.toHaveBeenCalledWith(expectedPath);

                            var updatedTribe = {_id: newTribeId};
                            callback(updatedTribe);
                            expect(location.path).toHaveBeenCalledWith(expectedPath);
                        });
                    });
                });
            });


            var EditTribeController = 'EditTribeController';
            describe(EditTribeController, function () {

                var Coupling, location, routeParams;
                beforeEach(function () {
                    location = {path: jasmine.createSpy('path')};
                    var selectedTribe = {name: 'Party tribe.', _id: 'party'};
                    Coupling = {data: {selectedTribe: selectedTribe}, selectTribe: jasmine.createSpy('selectTribe')};
                    routeParams = {tribeId: selectedTribe._id};
                });

                it('puts the selected tribe on the scope', function () {
                    var previouslyScopedTribe = {name: 'This should not be the tribe after injection.'};
                    scope.tribe = previouslyScopedTribe;
                    injectController(EditTribeController, scope, location, Coupling, routeParams);
                    expect(scope.tribe).not.toBe(previouslyScopedTribe);
                    expect(scope.tribe).toBe(Coupling.data.selectedTribe);
                });

                it('will deselect tribe', function () {
                    expect(Coupling.selectTribe).not.toHaveBeenCalled();
                    injectController(EditTribeController, scope, location, Coupling, routeParams);
                    expect(Coupling.selectTribe).toHaveBeenCalledWith(Coupling.data.selectedTribe._id);
                });

                describe('when pressing the save button ', function () {
                    beforeEach(function () {
                        Coupling.saveTribe = jasmine.createSpy('save tribe spy');
                        injectController(EditTribeController, scope, location, Coupling, routeParams);
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
                            var expectedPath = '/' + newTribeId + '/pairAssignments/current';
                            expect(location.path).not.toHaveBeenCalledWith(expectedPath);

                            var updatedTribe = {_id: newTribeId};
                            callback(updatedTribe);
                            expect(location.path).toHaveBeenCalledWith(expectedPath);
                        });
                    });
                });
            });

        });
    });
});
