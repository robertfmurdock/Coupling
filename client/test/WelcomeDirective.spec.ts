import * as angular from "angular";
import "ng-fittext";
import "../app/components/components";
import {Randomizer} from "../../client/app/services";
import * as _ from "underscore";
import Player from "../../common/Player";

describe('Welcome Directive:', function () {

    beforeEach(angular.mock.module('coupling'));

    describe('Welcome controller', function () {

        function buildDirective(randomValue,
                                timeout?) {
            let randomizer = new Randomizer();
            spyOn(randomizer, 'next').and.returnValue(randomValue);

            angular.mock.module(function ($provide) {
                $provide.value('randomizer', randomizer);
                if (timeout) {
                    $provide.value('$timeout', timeout);
                }
            });

            let statisticsDirective: JQuery = undefined;
            inject(function ($compile, $rootScope) {
                const element = angular.element('<welcomepage />');
                const scope = $rootScope.$new();
                statisticsDirective = $compile(element)(scope);

                scope.$digest();
            });

            return statisticsDirective;
        }

        it('does not show initially', function () {
            const welcomeElement = buildDirective(0);
            const scope: any = welcomeElement.scope();
            const controller = scope.welcome;
            expect(controller.show).toBe(false);
        });

        it('will show after a zero timeout so that the animation works', function () {
            let timeout: any = jasmine.createSpy('timeout');
            timeout.cancel = jasmine.createSpy('cancel');

            const welcomeElement = buildDirective(0, timeout);
            const scope: any = welcomeElement.scope();
            const controller = scope.welcome;

            _.each(timeout.calls.all(), function (call: any) {
                const [callback, waitTime] = call.args;
                if (waitTime === 0) {
                    callback()
                }
            });

            expect(controller.show).toBe(true);
        });

        it('will choose return hobbits when it rolls a zero.', function () {
            let randomValue = 0;
            const welcomeElement = buildDirective(randomValue);
            const scope: any = welcomeElement.scope();
            const controller = scope.welcome;

            const expectedLeftPlayer: Player = {
                _id: 'Frodo',
                name: 'Frodo',
                tribe: 'welcome',
                imageURL: '/images/icons/players/frodo-icon.png'
            };
            const expectedRightPlayer: Player = {
                _id: 'Sam',
                name: 'Sam',
                tribe: 'welcome',
                imageURL: '/images/icons/players/samwise-icon.png'
            };
            expect(controller.leftPlayer).toEqual(expectedLeftPlayer);
            expect(controller.rightPlayer).toEqual(expectedRightPlayer);
            expect(controller.proverb).toEqual('Together, climb mountains.');
        });

        it('will return the dynamic duo when it rolls a one.', function () {
            let randomValue = 1;
            const welcomeElement = buildDirective(randomValue);
            const scope: any = welcomeElement.scope();
            const controller = scope.welcome;

            const expectedLeftPlayer: Player = {
                _id: 'Batman',
                name: 'Batman',
                tribe: 'welcome',
                imageURL: '/images/icons/players/grayson-icon.png'
            };
            const expectedRightPlayer: Player = {
                _id: 'Robin',
                name: 'Robin',
                tribe: 'welcome',
                imageURL: '/images/icons/players/wayne-icon.png'
            };

            expect(controller.leftPlayer).toEqual(expectedLeftPlayer);
            expect(controller.rightPlayer).toEqual(expectedRightPlayer);
            expect(controller.proverb).toEqual('Clean up the city, together.');
        });

        it('will return the heros of WW II when it rolls a two.', function () {
            let randomValue = 2;
            const welcomeElement = buildDirective(randomValue);
            const scope: any = welcomeElement.scope();
            const controller = scope.welcome;

            const expectedLeftPlayer: Player = {
                _id: 'Rosie',
                name: 'Rosie',
                tribe: 'welcome',
                imageURL: '/images/icons/players/rosie-icon.png'
            };
            const expectedRightPlayer: Player = {
                _id: 'Wendy',
                name: 'Wendy',
                tribe: 'welcome',
                imageURL: '/images/icons/players/wendy-icon.png'
            };

            expect(controller.leftPlayer).toEqual(expectedLeftPlayer);
            expect(controller.rightPlayer).toEqual(expectedRightPlayer);
            expect(controller.proverb).toEqual('Team up. Get things done.');
        });
    });
});