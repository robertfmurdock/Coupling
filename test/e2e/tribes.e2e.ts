"use strict";
import {browser, element, By} from "protractor";
import * as _ from "underscore";
import * as monk from "monk";
import e2eHelp from "./e2e-help";

const config = require("../../config");
const hostName = 'http://' + config.publicHost + ':' + config.port;
const database = monk(config.tempMongoUrl);
const tribeCollection = database.get('tribes');

const userEmail = 'protractor@test.goo';

function authorizeAllTribes() {
    return tribeCollection.find({}, {})
        .then(function (tribeDocuments) {
            const authorizedTribes = _.pluck(tribeDocuments, 'id');
            return e2eHelp.authorizeUserForTribes(authorizedTribes);
        });
}

function waitUntilAnimateIsGone() {
    browser.wait(function () {
        return element(By.css('.ng-animate'))
            .isPresent()
            .then(isPresent => !isPresent, () => false);
    }, 5000);
}

const tribeCardStyles = require('../../client/app/components/tribe-card/styles.css');
const tribeCardHeaderLocator = By.className(tribeCardStyles.header);

const tribeConfigStyles = require('../../client/app/components/tribe-config/styles.css');
const tribeConfigElement = element(By.className(tribeConfigStyles.className));


const tribeListPage = {
    getTribeElements: function () {
        return element.all(By.repeater('tribe in tribes'));
    },
    getTribeNameLabel: function (tribeElement) {
        return tribeElement.element(tribeCardHeaderLocator);
    },
    getNewTribeButton: function () {
        return element(By.id('new-tribe-button'));
    }
};

describe('The default tribes page', function () {

    let tribeDocuments;

    beforeAll(function () {
        browser.driver.manage().deleteAllCookies();
        browser.wait(() => tribeCollection.drop()
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
        browser.get(hostName + '/test-login?username=' + userEmail + '&password="pw"');
    });

    beforeEach(function () {
        browser.setLocation('/tribes');
        expect(browser.getCurrentUrl()).toEqual(hostName + '/tribes/');
    });
    e2eHelp.afterEachAssertLogsAreEmpty();

    it('should have a section for each tribe', function () {
        const tribeElements = tribeListPage.getTribeElements();
        expect(tribeElements.getText()).toEqual(_.pluck(tribeDocuments, 'name'));
    });

    it('can navigate to the a specific tribe page', function () {
        const tribeElements = tribeListPage.getTribeElements();
        tribeListPage.getTribeNameLabel(tribeElements.first()).click();
        expect(browser.getCurrentUrl()).toEqual(hostName + '/' + tribeDocuments[0].id + '/edit/');
    });

    it('can navigate to the new tribe page', function () {
        tribeListPage.getNewTribeButton().click();
        expect(browser.getCurrentUrl()).toBe(hostName + '/new-tribe/');
    });

    describe('when a tribe exists, on the tribe page', function () {

        let expectedTribe;
        beforeAll(function () {
            expectedTribe = tribeDocuments[0];
            browser.setLocation('/' + expectedTribe.id + '/');
            element(By.tagName('body')).allowAnimations(false);
            waitUntilAnimateIsGone();
        });

        beforeEach(function () {
            browser.setLocation('/' + expectedTribe.id + '/edit/');
            expect(browser.getCurrentUrl()).toEqual(hostName + '/' + expectedTribe.id + '/edit/');
        });

        it('the tribe view is shown', function () {
            expect(tribeConfigElement.isDisplayed()).toBe(true);
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

