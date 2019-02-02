"use strict";
import {element, browser, By} from "protractor";
import e2eHelp from "./e2e-help";

const config = require("../../config/config");
const hostName = `http://${config.publicHost}:${config.port}`;

describe('The welcome page', function () {

    const pageBody = element(By.tagName('body'));
    const enterButton = element(By.className("enter-button"));
    const googleButton = element(By.className("google-login"));
    const microsoftButton = element(By.className("ms-login"));

    async function waitToArriveAt(expectedHost: string) {
        await browser.wait(async () => {
            try {
                const url = await browser.getCurrentUrl();

                return url.startsWith(expectedHost);
            } catch (e) {
                console.log(e);
                return false
            }
        }, 5000);

        browser.getCurrentUrl().then(function (url) {
            expect(url.startsWith(expectedHost)).toBe(true, `url was ${url}`);
        });
    }

    it('has an enter button redirects to google login', async function () {
        browser.get(hostName + '/welcome');
        pageBody.allowAnimations(false);

        enterButton.click();
        googleButton.click();

        browser.waitForAngularEnabled(false);

        let expectedHost = 'https://accounts.google.com';

        await waitToArriveAt(expectedHost);
    });

    it('has an enter button redirects to ms login', async function () {
        browser.get(hostName + '/welcome');
        pageBody.allowAnimations(false);

        enterButton.click();
        microsoftButton.click();

        browser.waitForAngularEnabled(false);

        let expectedHost = 'https://login.live.com';

        await waitToArriveAt(expectedHost);
    });

    afterEach(function () {
        browser.waitForAngularEnabled(true);
    });

    e2eHelp.deleteAnyBrowserLogging();
});