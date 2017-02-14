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

const tribeListPage = {
    getTribeElements: function () {
        return element.all(By.repeater('tribe in tribes'));
    },
    getTribeNameLabel: function (tribeElement) {
        return tribeElement.element(By.css('.tribe-name'));
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
            expect(element(By.css('.tribe-view')).isDisplayed()).toBe(true);
        });

        it('the tribe name is shown', function () {
            const tribeNameElement = element.all(By.id('tribe-name')).first();
            expect(tribeNameElement.getAttribute('value')).toEqual(expectedTribe.name);
        });

        it('the tribe image url is shown', function () {
            const tribeNameElement = element.all(By.id('tribe-img-url')).first();
            const expectedValue = expectedTribe.imgURL || '';
            expect(tribeNameElement.getAttribute('value')).toEqual(expectedValue);
        });

        it('the tribe email is shown', function () {
            const tribeNameElement = element.all(By.id('tribe-email')).first();
            const expectedValue = expectedTribe.email || '';
            expect(tribeNameElement.getAttribute('value')).toEqual(expectedValue);
        });
    });

    describe('on the new tribe page', function () {

        it('the id field shows and does not disappear when text is added', function () {
            browser.setLocation('/new-tribe/');
            const tribeIdElement = element(By.id('tribe-id'));
            tribeIdElement.sendKeys('oopsie');
            expect(tribeIdElement.isDisplayed()).toBe(true);
        });
    });
});

let updateTextBox = function (selector: any, text: string) {
    element(selector).clear();
    element(selector).sendKeys(text);
};
describe('The edit tribe page', function () {

    const tribe = {
        id: 'delete_me',
        name: 'Change Me'
    };

    beforeAll(function (done) {
        tribeCollection.drop()
            .then(() => tribeCollection.insert(tribe))
            .then(() => authorizeAllTribes())
            .then(done, done.fail);
    });

    afterAll(function (done) {
        tribeCollection.remove({id: tribe.id}, false)
            .then(done, done.fail);
    });

    e2eHelp.afterEachAssertLogsAreEmpty();

    it('can save edits to a tribe correctly', function () {
        browser.get(hostName + '/test-login?username=' + userEmail + '&password="pw"');
        const tribeElements = element.all(By.repeater('tribe in tribes'));
        tribeElements.first().element(By.css('.tribe-name')).click();

        expect(browser.getCurrentUrl()).toEqual(hostName + '/' + tribe.id + '/edit/');
        expect(element(By.id('tribe-name')).getAttribute('value')).toEqual(tribe.name);
        const expectedNewName = 'Different name';
        element(By.id('tribe-name')).clear();
        element(By.id('tribe-name')).sendKeys(expectedNewName);

        element(By.id('badge-checkbox')).click();
        const expectedDefaultBadgeName = 'New Default Badge Name';
        updateTextBox(By.id('default-badge-name'), expectedDefaultBadgeName);
        const expectedAltBadgeName = 'New Alt Badge Name';
        updateTextBox(By.id('alt-badge-name'), expectedAltBadgeName);

        element(By.id('save-tribe-button')).click();

        browser.setLocation('/' + tribe.id + '/edit/');

        expect(element(By.id('tribe-name')).getAttribute('value')).toEqual(expectedNewName);
        expect(element(By.id('badge-checkbox')).getAttribute('checked')).toEqual('true');
        expect(element(By.id('default-badge-name')).getAttribute('value')).toEqual(expectedDefaultBadgeName);
        expect(element(By.id('alt-badge-name')).getAttribute('value')).toEqual(expectedAltBadgeName);
    });
});