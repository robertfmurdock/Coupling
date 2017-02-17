import * as angular from "angular";
import "../../../client/app/components/components";
import {WelcomeController} from "../../../client/app/components/welcome/welcome";
import {Randomizer} from "../../../client/app/services";

describe('The controller named ', function () {

    beforeEach(function () {
        angular.module("coupling.component");
    });

    describe('Welcome controller', function () {
        let controller;

        function initWelcomeController(randomValue) {
            inject(function ($controller, $timeout) {
                let randomizer = new Randomizer();
                spyOn(randomizer, 'next').and.returnValue(randomValue);
                controller = new WelcomeController($timeout, randomizer);
            });
        }

        it('does not show initially', function () {
            inject(function ($timeout) {
                let randomizer = new Randomizer();
                controller = new WelcomeController($timeout, randomizer);
            });
            expect(controller.show).toBe(false);
        });

        it('will show after a zero timeout so that the animation works', function () {
            let timeout: any = jasmine.createSpy('timeout');
            timeout.cancel = jasmine.createSpy('cancel');

            let randomizer = new Randomizer();
            controller = new WelcomeController(timeout, randomizer);

            expect(timeout.calls.count()).toBe(1);
            let timeoutArgs = timeout.calls.argsFor(0);
            let waitTime = timeoutArgs[1];
            expect(waitTime).toBe(0);
            let callback = timeoutArgs[0];
            callback();
            expect(controller.show).toBe(true);
        });

        it('will choose return hobbits when it rolls a zero.', function () {
            let randomValue = 0;
            initWelcomeController(randomValue);
            expect(controller.leftCard).toEqual({
                name: 'Frodo',
                imagePath: 'frodo-icon.png'
            });
            expect(controller.rightCard).toEqual({
                name: 'Sam',
                imagePath: 'samwise-icon.png'
            });
            expect(controller.proverb).toEqual('Together, climb mountains.');
        });

        it('will return the dynamic duo when it rolls a one.', function () {
            let randomValue = 1;
            initWelcomeController(randomValue);
            expect(controller.leftCard).toEqual({
                name: 'Batman',
                imagePath: 'grayson-icon.png'
            });
            expect(controller.rightCard).toEqual({
                name: 'Robin',
                imagePath: 'wayne-icon.png'
            });
            expect(controller.proverb).toEqual('Clean up the city, together.');
        });

        it('will return the heros of WW II when it rolls a two.', function () {
            let randomValue = 2;
            initWelcomeController(randomValue);
            expect(controller.leftCard).toEqual({
                name: 'Rosie',
                imagePath: 'rosie-icon.png'
            });
            expect(controller.rightCard).toEqual({
                name: 'Wendy',
                imagePath: 'wendy-icon.png'
            });
            expect(controller.proverb).toEqual('Team up. Get things done.');
        });
    });
});