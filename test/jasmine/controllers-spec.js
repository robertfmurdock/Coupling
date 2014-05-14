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

            describe('TribeListController', function () {
                function injectController(controllerName, scope, location, Coupling) {
                    inject(function ($controller) {
                        $controller(controllerName, {
                            $scope: scope,
                            $location: location,
                            Coupling: Coupling
                        });
                    });
                }

                var Coupling, location;
                beforeEach(function () {
                    location = {path: jasmine.createSpy('path')};
                    Coupling = {data: {}, selectTribe: jasmine.createSpy('selectTribe')};
                });

                it('will deselect tribe', function () {
                    expect(Coupling.selectTribe).not.toHaveBeenCalled();
                    injectController('TribeListController', scope, location, Coupling);
                    expect(Coupling.selectTribe).toHaveBeenCalledWith(null);
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
        });
    });
});
