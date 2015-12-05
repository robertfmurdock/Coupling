"use strict";

describe('The controller named ', function () {

  beforeEach(function () {
    module("coupling.controllers");
  });

  describe('Welcome controller', function () {
    var controller;

    function initWelcomeController(randomValue) {
      inject(function ($controller, randomizer) {
        spyOn(randomizer, 'next').and.returnValue(randomValue);
        controller = $controller('WelcomeController', {
          randomizer: randomizer
        });
      });
    }

    it('does not show initially', function () {
      inject(function ($controller) {
        controller = $controller('WelcomeController', {});
      });
      expect(controller.show).toBe(false);
    });

    it('will show after a zero timeout so that the animation works', function () {
      var timeout = jasmine.createSpy('timeout');

      inject(function ($controller) {
        controller = $controller('WelcomeController', {
          $timeout: timeout
        });
      });

      expect(timeout.calls.count()).toBe(1);
      var timeoutArgs = timeout.calls.argsFor(0);
      var waitTime = timeoutArgs[1];
      expect(waitTime).toBe(0);
      var callback = timeoutArgs[0];
      callback();
      expect(controller.show).toBe(true);
    });

    it('will choose return hobbits when it rolls a zero.', function () {
      var randomValue = 0;
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
      var randomValue = 1;
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
      var randomValue = 2;
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