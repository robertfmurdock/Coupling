"use strict";
import {element, browser, By} from "protractor";
import e2eHelp from "./e2e-help";

const config = require("../../server/config/config");
const hostName = `http://${config.publicHost}:${config.port}`;

describe('The welcome page', function () {

    const pageBody = element(By.tagName('body'));
    const enterButton = element(By.className("enter-button"));

    it('has an enter button redirects to google login', async function () {
        browser.get(hostName + '/welcome');
        pageBody.allowAnimations(false);

        enterButton.click();
        browser.waitForAngularEnabled(false);

        await browser.wait(async () => {
            try {
                const url = await browser.getCurrentUrl();
                return url.startsWith('https://accounts.google.com');
            } catch (e) {
                console.log(e);
                return false
            }
        }, 5000);

        browser.getCurrentUrl().then(function (url) {
            expect(url.startsWith('https://accounts.google.com')).toBe(true, `url was ${url}`);
        });

    });

    afterEach(function () {
        browser.waitForAngularEnabled(true);
    });

    e2eHelp.deleteAnyBrowserLogging();
});