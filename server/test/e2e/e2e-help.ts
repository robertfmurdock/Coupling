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

const helper = {
    userEmail: userEmail,
    authorizeUserForTribes: authorizeUserForTribes,
    afterEachAssertLogsAreEmpty: function () {

        afterEach(function (done) {
            browser.getCapabilities()
                .then(function (capabilities) {
                    if (capabilities.get('browserName') !== 'firefox') {
                        browser.manage().logs().get('browser').then(function (browserLog) {
                            expect(browserLog).toEqual([]);
                            if (browserLog.length > 0) {
                                console.log('log: ' + util.inspect(browserLog));
                            }
                            done();
                        }, done);
                        browser.waitForAngular();
                    } else {
                        done();
                    }
                });
        });
    },
    deleteAnyBrowserLogging: function () {

        afterEach(function (done) {
            browser.getCapabilities()
                .then(function (capabilities) {
                    if (capabilities.get('browserName') !== 'firefox') {
                        browser.manage().logs().get('browser').then(done, done.fail);
                        browser.waitForAngular();
                    } else {
                        done();
                    }
                });
        });
    }
};

export default helper;