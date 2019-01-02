"use strict";
import {element, browser, By} from "protractor";
import e2eHelp from "./e2e-help";

const config = require("../../server/config/config");
const hostName = `http://${config.publicHost}:${config.port}`;

const welcomeStyles = require('../../client/app/components/welcome/styles.css');

describe('The welcome page', function () {

    const pageBody = element(By.tagName('body'));
    const enterButton = element(By.className(welcomeStyles.enterButton));

    it('has an enter button redirects to google login', async function () {
        browser.get(hostName + '/welcome');
        pageBody.allowAnimations(false);

        await enterButton.click();
        browser.waitForAngularEnabled(false);


        await browser.wait(async () => {
            try {
                const handles = await browser.getAllWindowHandles();
                if(handles.length < 2) {
                    return false;
                }
                // Give this a moment to let the google auth code load. It seems if protractor gets to it too soon, it does not load.
                browser.sleep(100);
                await browser.switchTo().window(handles[1]);
                const url = await browser.getCurrentUrl();
                return url.startsWith('https://accounts.google.com');
            } catch (e) {
                console.log(e);
                return false
            }
        }, 5000);

        await browser.getCurrentUrl().then(function (url) {
            expect(url.startsWith('https://accounts.google.com')).toBe(true);
        });

        await browser.close();
    });

    afterEach(function () {
        browser.waitForAngularEnabled(true);
    });

    e2eHelp.deleteAnyBrowserLogging();
});