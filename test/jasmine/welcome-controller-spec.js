"use strict";

describe('The controller named ', function () {

    beforeEach(function () {
        module("coupling.controllers");
    });

    describe('Welcome controller', function () {

        function initWelcomeController(scope, randomValue) {
            inject(function ($controller, randomizer) {
                spyOn(randomizer, 'next').and.returnValue(randomValue);
                $controller('WelcomeController', {
                    $scope: scope,
                    randomizer: randomizer
                });
            });
        }

        it('does not show initially', function () {
            var scope = {};
            inject(function ($controller) {
                $controller('WelcomeController', {
                    $scope: scope
                });
            });
            expect(scope.show).toBe(false);
        });

        it('will show after a zero timeout so that the animation works', function () {
            var scope = {};
            var timeout = jasmine.createSpy('timeout');

            inject(function ($controller) {
                $controller('WelcomeController', {
                    $scope: scope,
                    $timeout: timeout
                });
            });

            expect(timeout.calls.count()).toBe(1);
            var timeoutArgs = timeout.calls.argsFor(0);
            var waitTime = timeoutArgs[1];
            expect(waitTime).toBe(0);
            var callback = timeoutArgs[0];
            callback();
            expect(scope.show).toBe(true);
        });

        it('will choose return hobbits when it rolls a zero.', function () {
            var scope = {};
            var randomValue = 0;
            initWelcomeController(scope, randomValue);
            expect(scope.leftCard).toEqual({
                name: 'Frodo',
                imagePath: 'frodo-icon.png'
            });
            expect(scope.rightCard).toEqual({
                name: 'Sam',
                imagePath: 'samwise-icon.png'
            });
            expect(scope.proverb).toEqual('Together, climb mountains.');
        });

        it('will return the dynamic duo when it rolls a one.', function () {
            var scope = {};
            var randomValue = 1;
            initWelcomeController(scope, randomValue);
            expect(scope.leftCard).toEqual({
                name: 'Batman',
                imagePath: 'grayson-icon.png'
            });
            expect(scope.rightCard).toEqual({
                name: 'Robin',
                imagePath: 'wayne-icon.png'
            });
            expect(scope.proverb).toEqual('Clean up the city, together.');
        });

        it('will return the heros of WW II when it rolls a two.', function () {
            var scope = {};
            var randomValue = 2;
            initWelcomeController(scope, randomValue);
            expect(scope.leftCard).toEqual({
                name: 'Rosie',
                imagePath: 'rosie-icon.png'
            });
            expect(scope.rightCard).toEqual({
                name: 'Wendy',
                imagePath: 'wendy-icon.png'
            });
            expect(scope.proverb).toEqual('Team up. Get things done.');
        });
    });
});