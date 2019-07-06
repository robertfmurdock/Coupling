"use strict";
import {element, browser, By} from "protractor";
import e2eHelp from "./e2e-help";
import WelcomePage from "./page-objects/WelcomePage";

const config = require("../../config/config");
const hostName = `http://${config.publicHost}:${config.port}`;

describe('The welcome page', function () {

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
        await WelcomePage.goTo();

        WelcomePage.enterButton.click();
        WelcomePage.googleButton.click();

        let expectedHost = 'https://accounts.google.com';

        await waitToArriveAt(expectedHost);
    });

    it('has an enter button redirects to ms login', async function () {
        await WelcomePage.goTo();

        WelcomePage.enterButton.click();
        WelcomePage.microsoftButton.click();

        let expectedHostThatAllowsMultiTenantAuth = 'https://login.microsoftonline.com';

        await waitToArriveAt(expectedHostThatAllowsMultiTenantAuth);
    });

    e2eHelp.deleteAnyBrowserLogging();
});