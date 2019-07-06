"use strict";
import {browser, element, By} from "protractor";
import * as monk from "monk";
import e2eHelp from "./e2e-help";
import * as pluck from 'ramda/src/pluck'

import TribeListPage from './page-objects/TribeListPage'
import TestLogin from "./TestLogin";
import TribeConfigPage from "./page-objects/TribeConfigPage";

const config = require("../../config/config");
const hostName = 'http://' + config.publicHost + ':' + config.port;
const database = monk.default(config.tempMongoUrl);
const tribeCollection = database.get('tribes');

function authorizeAllTribes() {
    return tribeCollection.find({}, {})
        .then(function (tribeDocuments) {
            const authorizedTribes = pluck('id', tribeDocuments);
            return e2eHelp.authorizeUserForTribes(authorizedTribes);
        });
}

describe('The default tribes page', function () {

    let tribeDocuments;

    beforeAll(async function () {
        await browser.driver.manage().deleteAllCookies();
        await browser.wait(() => tribeCollection.drop()
            .then(() => tribeCollection.insert([
                {
                    id: 'e2e1',
                    name: 'E2E Example Tribe 1'
                }, {
                    id: 'e2e2',
                    name: 'E2E Example Tribe 2'
                }
            ]))
            .then(() => authorizeAllTribes())
            .then(() => tribeCollection.find({}, {}))
            .then(tribesInCollection => {
                tribeDocuments = tribesInCollection;
                return true;
            }));
        await TestLogin.login();
    });

    beforeEach(async function () {
        await TribeListPage.goTo();
        expect(browser.getCurrentUrl()).toEqual(hostName + '/tribes/');
    });

    e2eHelp.afterEachAssertLogsAreEmpty();
    
    it('should have a section for each tribe', function () {
        const tribeElements = TribeListPage.getTribeElements();
        expect(tribeElements.getText()).toEqual(pluck('name', tribeDocuments));
    });

    it('can navigate to the a specific tribe page', function () {
        const tribeElements = TribeListPage.getTribeElements();
        TribeListPage.getTribeNameLabel(tribeElements.first()).click();
        expect(browser.getCurrentUrl()).toEqual(hostName + '/' + tribeDocuments[0].id + '/edit/');
    });

    it('can navigate to the new tribe page', function () {
        TribeListPage.getNewTribeButton().click();
        expect(browser.getCurrentUrl()).toBe(hostName + '/new-tribe/');
    });

    describe('when a tribe exists, on the tribe page', function () {

        let expectedTribe;
        beforeAll(async function () {
            expectedTribe = tribeDocuments[0];
        });

        beforeEach(async function () {
            await TribeConfigPage.goTo(expectedTribe.id);
            expect(browser.getCurrentUrl()).toEqual(`${hostName}/${expectedTribe.id}/edit/`);
        });

        it('the tribe view is shown', function () {
            expect(TribeConfigPage.tribeConfigElement.isDisplayed()).toBe(true);
        });

        it('the tribe name is shown', function () {
            const tribeNameElement = element.all(By.id('tribe-name')).first();
            expect(tribeNameElement.getAttribute('value')).toEqual(expectedTribe.name);
        });

        it('the tribe email is shown', function () {
            const tribeNameElement = element.all(By.id('tribe-email')).first();
            const expectedValue = expectedTribe.email || '';
            expect(tribeNameElement.getAttribute('value')).toEqual(expectedValue);
        });

    });

});

