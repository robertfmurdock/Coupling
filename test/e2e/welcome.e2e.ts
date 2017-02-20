"use strict";
import {element, browser, By} from "protractor";
import e2eHelp from "./e2e-help";

const config = require("../../config");
const hostName = `http://${config.publicHost}:${config.port}`;

describe('The welcome page', function () {

    it('has an enter button redirects to google login', function () {
        browser.get(hostName + '/welcome');
        element(By.tagName('body')).allowAnimations(false);
        element(By.id('enter-button')).click();
        browser.waitForAngularEnabled(false);

        browser.getCurrentUrl().then(function (url) {
            expect(url.startsWith('https://accounts.google.com/ServiceLogin')).toBe(true);
        });

    });

    afterEach(function () {
        browser.waitForAngularEnabled(true);
    });

    e2eHelp.afterEachAssertLogsAreEmpty();
});