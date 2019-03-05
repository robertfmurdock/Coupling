import {browser, element, By} from "protractor";
import * as monk from "monk";
import e2eHelp from "./e2e-help";
import * as pluck from 'ramda/src/pluck'

const config = require("../../config/config");
const hostName = 'http://' + config.publicHost + ':' + config.port;
const database = monk.default(config.tempMongoUrl);
const tribeCollection = database.get('tribes');

const userEmail = 'protractor@test.goo';

function authorizeAllTribes() {
    return tribeCollection.find({}, {})
        .then(function (tribeDocuments) {
            const authorizedTribes = pluck('id', tribeDocuments);
            return e2eHelp.authorizeUserForTribes(authorizedTribes);
        });
}

let updateTextBox = function (selector: any, text: string) {
    element(selector).clear();
    element(selector).sendKeys(text);
};

describe('The edit tribe page', function () {

    const checkedOption = element(By.css('#pairing-rule option:checked'));
    const tribeIdElement = element(By.id('tribe-id'));

    const deleteButton = element(By.className('delete-tribe-button'));

    describe('for an existing tribe', function () {
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

        it('can save edits to a tribe correctly', async function () {
            await browser.get(hostName + '/test-login?username=' + userEmail + '&password="pw"');

            await browser.wait(async () => `${hostName}/tribes/` === await browser.getCurrentUrl(), 1000);
            const tribeElements = element.all(By.repeater('tribe in tribeList.tribes'));
            tribeElements.first().element(By.className("tribe-card-header")).click();

            expect(browser.getCurrentUrl()).toEqual(hostName + '/' + tribe.id + '/edit/');
            expect(element(By.id('tribe-name')).getAttribute('value')).toEqual(tribe.name);
            const expectedNewName = 'Different name';
            element(By.id('tribe-name')).clear();
            element(By.id('tribe-name')).sendKeys(expectedNewName);

            element(By.id('call-sign-checkbox')).click();
            element(By.id('badge-checkbox')).click();
            const expectedDefaultBadgeName = 'New Default Badge Name';
            updateTextBox(By.id('default-badge-name'), expectedDefaultBadgeName);
            const expectedAltBadgeName = 'New Alt Badge Name';
            updateTextBox(By.id('alt-badge-name'), expectedAltBadgeName);
            const differentBadgesOption = element(By.css('#pairing-rule option[label="Prefer Different Badges (Beta)"]'));
            differentBadgesOption.click();

            element(By.id('save-tribe-button')).click();
            await browser.wait(async () => `${hostName}/tribes/` === await browser.getCurrentUrl(), 1000);

            await browser.setLocation('/' + tribe.id + '/edit/');
            await browser.wait(() => element(By.id('tribe-name')).isPresent(), 2000);

            expect(element(By.id('tribe-name')).getAttribute('value')).toEqual(expectedNewName);
            expect(element(By.id('call-sign-checkbox')).getAttribute('checked')).toEqual('true');
            expect(element(By.id('badge-checkbox')).getAttribute('checked')).toEqual('true');
            expect(element(By.id('default-badge-name')).getAttribute('value')).toEqual(expectedDefaultBadgeName);
            expect(element(By.id('alt-badge-name')).getAttribute('value')).toEqual(expectedAltBadgeName);
            expect(checkedOption.getAttribute('label')).toBe('Prefer Different Badges (Beta)');
        });

        it('the tribe can be deleted', async function() {
            await browser.get(hostName + '/test-login?username=' + userEmail + '&password="pw"');

            await browser.wait(async () => `${hostName}/tribes/` === await browser.getCurrentUrl(), 1000);
            const tribeElements = element.all(By.repeater('tribe in tribeList.tribes'));
            tribeElements.first().element(By.className("tribe-card-header")).click();

            await expect(browser.getCurrentUrl()).toEqual(hostName + '/' + tribe.id + '/edit/');

            await expect(deleteButton.isDisplayed()).toBe(true);
            await expect(deleteButton.isEnabled()).toBe(true);

            deleteButton.click();

            await browser.wait(async () => `${hostName}/tribes/` === await browser.getCurrentUrl(), 1000);

            await expect(tribeElements.count()).toBe(0);
        });
    });

    describe('on the new tribe page', function () {

        beforeAll(function () {
            browser.setLocation('/new-tribe/');
        });

        it('the id field shows and does not disappear when text is added', function () {
            tribeIdElement.sendKeys('oopsie');
            expect(tribeIdElement.isDisplayed()).toBe(true);
        });

        it('will default the pairing rule to Longest Time', function () {
            expect(checkedOption.getAttribute('label')).toBe('Prefer Longest Time');
        });
    });

});
