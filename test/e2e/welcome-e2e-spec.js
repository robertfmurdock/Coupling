"use strict";
var monk = require("monk");
var _ = require('underscore');
var config = require("../../config");
var hostName = 'http://' + config.publicHost + ':' + config.port;
var database = monk(config.tempMongoUrl);

describe('The welcome page', function () {

    it('will have a clickable enter button', function () {
        browser.get(hostName + '/welcome');
        element(By.tagName('body')).allowAnimations(false);
        element(By.id('enter-button')).click();
        expect(browser.getCurrentUrl()).toBe(hostName + '/auth/google');
    });
});