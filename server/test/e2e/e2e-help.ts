"use strict";
import {browser} from "protractor";
import * as util from "util";
import {usersCollection} from "./database";

const userEmail = 'protractor@test.goo';

async function authorizeUserForTribes(authorizedTribes) {
    const tempUserEmail = userEmail + "._temp";
    await usersCollection.remove({email: tempUserEmail});
    await usersCollection.insert({email: tempUserEmail, tribes: authorizedTribes, timestamp: new Date()});
    return null
}

async function clearBrowserLogs() {
    const capabilities = await browser.getCapabilities();
    if (capabilities.get('browserName') !== 'firefox') {
        await browser.manage().logs().get('browser');
    }
}

const helper = {
    userEmail: userEmail,
    authorizeUserForTribes: authorizeUserForTribes,
    afterEachAssertLogsAreEmpty: function () {

        afterEach(async function () {
            const capabilities = await browser.getCapabilities();
            if (capabilities.get('browserName') !== 'firefox') {
                const browserLog = await browser.manage().logs().get('browser');
                expect(browserLog).toEqual([]);
                if (browserLog.length > 0) {
                    console.log('log: ' + util.inspect(browserLog));
                }
            }
        });
    },
    deleteAnyBrowserLogging: function () {

        afterEach(async function () {
            await clearBrowserLogs();
        });
    },
    clearBrowserLogs
};

export default helper;