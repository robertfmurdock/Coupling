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
            var scope = {unique: "value"};
            var location = {path: jasmine.createSpy('path')};
            var Coupling = {data: {}};
            beforeEach(couplingControllerFactory(scope, location, Coupling));

            it('will not redirect', function(){
                expect(location.path.calls.count()).toEqual(1);
            });

            it('will add data to the scope', function () {
                expect(scope.data).toBe(Coupling.data);
            });

            it('will start with all players selected', function () {
                expect(scope.deselectionMap).toBeDefined();
                expect(scope.deselectionMap.length).toEqual(0);
            });
        });
    });
});
