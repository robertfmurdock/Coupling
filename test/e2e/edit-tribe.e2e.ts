import {browser, element, By} from "protractor";
import * as _ from "underscore";
import * as monk from "monk";
import e2eHelp from "./e2e-help";

const config = require("../../config");
const hostName = 'http://' + config.publicHost + ':' + config.port;
const database = monk(config.tempMongoUrl);
const tribeCollection = database.get('tribes');

const tribeCardStyles = require('../../client/app/components/tribe-card/styles.css');

const userEmail = 'protractor@test.goo';

function authorizeAllTribes() {
    return tribeCollection.find({}, {})
        .then(function (tribeDocuments) {
            const authorizedTribes = _.pluck(tribeDocuments, 'id');
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

        it('can save edits to a tribe correctly', function () {
            browser.get(hostName + '/test-login?username=' + userEmail + '&password="pw"');
            const tribeElements = element.all(By.repeater('tribe in tribeList.tribes'));
            tribeElements.first().element(By.className(tribeCardStyles.header)).click();

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
            const differentBadgesOption = element(By.css('#pairing-rule option[label="Prefer Different Badges (Beta)"]'));
            differentBadgesOption.click();

            element(By.id('save-tribe-button')).click();

            browser.setLocation('/' + tribe.id + '/edit/');

            expect(element(By.id('tribe-name')).getAttribute('value')).toEqual(expectedNewName);
            expect(element(By.id('badge-checkbox')).getAttribute('checked')).toEqual('true');
            expect(element(By.id('default-badge-name')).getAttribute('value')).toEqual(expectedDefaultBadgeName);
            expect(element(By.id('alt-badge-name')).getAttribute('value')).toEqual(expectedAltBadgeName);
            expect(checkedOption.getAttribute('label')).toBe('Prefer Different Badges (Beta)');
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
