"use strict";
import {element, browser, By} from "protractor";
import e2eHelp from "./e2e-help";

const config = require("../../config");
const hostName = `http://${config.publicHost}:${config.port}`;

const welcomeStyles = require('../../client/app/components/welcome/styles.css');

describe('The welcome page', function () {

    const pageBody = element(By.tagName('body'));
    const enterButton = element(By.className(welcomeStyles.enterButton));

    it('has an enter button redirects to google login', function () {
        browser.get(hostName + '/welcome');
        pageBody.allowAnimations(false);

        enterButton.click();
        browser.waitForAngularEnabled(false);

        browser.getCurrentUrl().then(function (url) {
            expect(url.startsWith('https://accounts.google.com/ServiceLogin')).toBe(true);
        });

    });

    afterEach(function () {
        browser.waitForAngularEnabled(true);
    });

    e2eHelp.deleteAnyBrowserLogging();
});