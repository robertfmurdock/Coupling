"use strict";
var monk = require("monk");
var _ = require('underscore');
var config = require("../../config");

var hostName = 'http://localhost:' + config.port;
var database = monk(config.tempMongoUrl);

ddescribe('The welcome page', function () {

    it('will have a clickable enter button', function () {
        browser.ignoreSynchronization = false;
        browser.get(hostName + '/welcome');
        element(By.id('enter-button')).click();
        expect(browser.getCurrentUrl()).toBe(hostName + '/auth/google');
    });
});