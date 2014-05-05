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
            couplingControllerFactory(scope, location, Coupling);
            expect(pathSpy).toHaveBeenCalledWith('/tribes');
        });

        function couplingControllerFactory(scope, location, Coupling) {
            return inject(function ($controller) {
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
                couplingControllerFactory(scope, location, Coupling)
            });

            it('will not redirect', function () {
                expect(location.path.calls.count()).toEqual(1);
            });

            it('will add data to the scope', function () {
                expect(scope.data).toBe(Coupling.data);
            });

            it('will start with all players selected', function () {
                expect(scope.deselectionMap).toBeDefined();
                expect(scope.deselectionMap.length).toEqual(0);
            });

            describe('spin', function () {
                it('will redirect to the new pair assignments page', function () {
                    expect(location.path).not.toHaveBeenCalledWith(Coupling.data.selectedTribeId + '/pairAssignments/new');
                    scope.spin();
                    expect(location.path).toHaveBeenCalledWith(Coupling.data.selectedTribeId + '/pairAssignments/new');
                });
            });

            describe('the hide players state', function () {
                it('starts as default', function () {
                    expect(scope.hidePlayers).toBe(false);
                });

                it('can be flipped by calling showOrHidePlayers', function () {
                    scope.showOrHidePlayers();
                    expect(scope.hidePlayers).toBe(true);
                    scope.showOrHidePlayers();
                    expect(scope.hidePlayers).toBe(false);
                    scope.showOrHidePlayers();
                    expect(scope.hidePlayers).toBe(true);
                });

                it('can be set via a setter', function () {
                    scope.setHidePlayers(true);
                    expect(scope.hidePlayers).toBe(true);
                    scope.setHidePlayers(true);
                    expect(scope.hidePlayers).toBe(true);
                    scope.setHidePlayers(false);
                    expect(scope.hidePlayers).toBe(false);
                });
            });

            describe('view player', function () {
                it('will redirect to the players page', function () {
                    var id = 'PrettyGreatPlayerId';
                    var expectedPath = '/' + Coupling.data.selectedTribeId + '/player/' + id;
                    expect(location.path).not.toHaveBeenCalledWith(expectedPath);
                    var event = {};
                    scope.viewPlayer(id, event);
                    expect(location.path).toHaveBeenCalledWith(expectedPath);
                });

                it('will stop propagation to other click events', function () {
                    var id = 'PrettyGreatPlayerId';
                    var event = {stopPropagation: jasmine.createSpy('stopPropagation')};
                    scope.viewPlayer(id, event);
                    expect(event.stopPropagation).toHaveBeenCalled();
                });
            });

            describe('flip selection', function () {
                it('will change a players selection in the map', function () {
                    var player = {name: 'Chad', _id: 'merp'};
                    expect(scope.deselectionMap[player._id]).toBeFalsy();
                    scope.flipSelection(player);
                    expect(scope.deselectionMap[player._id]).toBeTruthy();
                    scope.flipSelection(player);
                    expect(scope.deselectionMap[player._id]).toBeFalsy();
                });
            });
        });
    });
});
