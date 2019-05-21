"use strict";
import {browser, By, element} from "protractor";
import {playersCollection, tribeCollection} from "./database";
import e2eHelp from "./e2e-help";
import Tribe from "../../../common/Tribe";
import * as monk from "monk";
import * as clone from "ramda/src/clone";
import * as pluck from "ramda/src/pluck";

const config = require("../../config/config");
const hostName = `http://${config.publicHost}:${config.port}`;

describe('The edit player page', function () {

    const tribe = {
        _id: monk.id(),
        id: 'delete_me',
        name: 'Change Me'
    };

    const player1 = {_id: monk.id(), tribe: tribe.id, name: "player1"};
    const player2 = {_id: monk.id(), tribe: tribe.id, name: "player2"};
    const player3 = {_id: monk.id(), tribe: tribe.id, name: "player3"};
    const player4 = {_id: monk.id(), tribe: tribe.id, name: "player4"};
    const player5 = {_id: monk.id(), tribe: tribe.id, name: "player5"};
    const players = [
        player1,
        player2,
        player3,
        player4,
        player5
    ];

    const tribeCardElement = element(By.className("tribe-card"));
    const deleteButton = element(By.className('delete-button'));
    const savePlayerButton = element(By.id('save-player-button'));

    async function waitForSaveToComplete(expectedName) {
        await browser.wait(() => savePlayerButton.isEnabled().then(value => value, () => false), 1000, 'wait for enable');

        await browser.wait(async () => {
            let currentValue = await element.all(By.css('.player-roster .player-card-header')).first().getText();
            return currentValue === expectedName;
        }, 100);
    }

    beforeAll(function (done) {
        tribeCollection.drop()
            .then(() => tribeCollection.insert(tribe))
            .then(() => e2eHelp.authorizeUserForTribes([tribe.id]))
            .then(done, done.fail);
    });

    beforeAll(function () {
        browser.get(`${hostName}/test-login?username=${e2eHelp.userEmail}&password="pw"`);
    });

    beforeEach(function (done) {
        playersCollection.drop()
            .then(() => playersCollection.insert(players))
            .then(done, done.fail);
    });

    afterAll(function (done) {
        tribeCollection.remove({id: tribe.id}, false)
            .then(() => playersCollection.drop())
            .then(done, done.fail);
    });

    e2eHelp.afterEachAssertLogsAreEmpty();

    it('should not alert on leaving when nothing has changed.', async function () {
        browser.setLocation(`/${tribe.id}/player/${player1._id}`);
        tribeCardElement.click();
        expect(browser.getCurrentUrl()).toBe(`${hostName}/${tribe.id}/pairAssignments/current/`);
        await browser.waitForAngular();
    });

    it('retire player should have intended effect.', async function () {
        browser.setLocation(`/${tribe.id}/player/${player1._id}`);
        await browser.waitForAngular();

        deleteButton.click();
        const alert = await browser.switchTo().alert();

        await alert.accept();

        await browser.waitForAngular();

        browser.wait(async function () {
            return await browser.getCurrentUrl() === `${hostName}/${tribe.id}/pairAssignments/current/`
        }, 100);
    });

    describe('when the tribe does not have badging enabled', function () {

        beforeEach(async function () {
            const tribeClone: Tribe = clone(tribe);
            tribeClone.badgesEnabled = false;
            await tribeCollection.update({_id: tribe._id}, tribeClone)
        });

        it('should not show the badge selector', function () {
            browser.setLocation(`/${tribe.id}/player/${player1._id}`);
            const defaultBadgeRadio = element(By.css('#default-badge-radio'));
            expect(defaultBadgeRadio.isDisplayed()).toEqual(false);
            const altBadgeRadio = element(By.css('#alt-badge-radio'));
            expect(altBadgeRadio.isDisplayed()).toEqual(false);
        });
    });

    describe('when the tribe does have badging enabled', function () {

        const defaultBadgeRadio = element(By.css('#default-badge-radio'));
        const altBadgeRadio = element(By.css('#alt-badge-radio'));

        beforeEach(async function () {
            const tribeClone: Tribe = clone(tribe);
            tribeClone.badgesEnabled = true;
            tribeClone.defaultBadgeName = "Badge 1";
            tribeClone.alternateBadgeName = "Badge 2";
            await tribeCollection.update({_id: tribe._id}, tribeClone)
        });

        it('should show the badge selector', async function () {
            await browser.setLocation(`/${tribe.id}/player/${player1._id}`);
            expect(defaultBadgeRadio.isDisplayed()).toEqual(true);
            expect(element(By.css('label[for=default-badge-radio]')).getText()).toBe('Badge 1');

            expect(altBadgeRadio.isDisplayed()).toEqual(true);
            expect(element(By.css('label[for=alt-badge-radio]')).getText()).toBe('Badge 2');
        });

        it('the player default badge should be selected', function () {
            expect(defaultBadgeRadio.getAttribute('checked')).toBe('true');
        });

        it(`should remember badge selection`, async function () {
            await browser.setLocation(`/${tribe.id}/player/${player1._id}`);
            await browser.wait(() => altBadgeRadio.isPresent(), 1000);

            await altBadgeRadio.click();
            await savePlayerButton.click();
            await waitForSaveToComplete(player1.name);
            await browser.setLocation(`/${tribe.id}/player/${player1._id}`);
            expect(altBadgeRadio.getAttribute('checked')).toBe('true');
        });

    });

    describe('when the tribe has call signs enabled', function () {

        const adjectiveTextInput = element(By.css('#adjective-input'));
        const nounTextInput = element(By.css('#noun-input'));

        beforeAll(async function () {
            const tribeClone: Tribe = clone(tribe);
            tribeClone.callSignsEnabled = true;
            await tribeCollection.update({_id: tribe._id}, tribeClone)
        });

        it(`should allow entry of adjective and noun, and retain them`, async function () {
            await browser.setLocation(`/${tribe.id}/player/${player1._id}`);
            await browser.wait(() => adjectiveTextInput.isPresent(), 1000);

            await adjectiveTextInput.clear();
            await adjectiveTextInput.sendKeys('Superior');
            await nounTextInput.clear();
            await nounTextInput.sendKeys('Spider-Man');
            await savePlayerButton.click();
            await waitForSaveToComplete(player1.name);
            await browser.setLocation(`/${tribe.id}/player/${player1._id}`);

            expect(adjectiveTextInput.getAttribute('value')).toBe('Superior');
            expect(nounTextInput.getAttribute('value')).toBe('Spider-Man');
        });

    });

    fit('should get error on leaving when name is changed.', async function () {
        await browser.setLocation(`/${tribe.id}/player/${player1._id}`);
        expect(browser.getCurrentUrl()).toBe(`${hostName}/${tribe.id}/player/${player1._id}/`);
        element(By.id('player-name')).clear();
        element(By.id('player-name')).sendKeys('completely different name');
        element(By.css(`.tribe-card img`)).click();
        await browser.wait(() => browser.switchTo().alert().then(() => true, () => false), 5000);

        const alertDialog = await browser.switchTo().alert();
        expect(alertDialog.getText())
            .toEqual('You have unsaved data. Would you like to save before you leave?');
        alertDialog.dismiss();
    });

    it('should not get alert on leaving when name is changed after save.', async function () {
        await browser.setLocation(`/${tribe.id}/player/${player1._id}`);
        const playerNameTextField = element(By.id('player-name'));
        playerNameTextField.clear();
        playerNameTextField.sendKeys('completely different name');

        await savePlayerButton.click();

        const expectedName = 'completely different name';

        await waitForSaveToComplete(expectedName);

        await browser.wait(() =>
            tribeCardElement.click()
                .then(() => true, () => false), 2000);

        expect(browser.getCurrentUrl()).toBe(`${hostName}/${tribe.id}/pairAssignments/current/`);

        await browser.setLocation(`/${tribe.id}/player/${player1._id}`);
        expect(element(By.id('player-name')).getAttribute('value')).toBe('completely different name')
    });

    it('saving with no name will show as a default name.', async function () {
        await browser.setLocation(`/${tribe.id}/player/${player1._id}`);
        const playerNameTextField = element(By.id('player-name'));
        playerNameTextField.clear();
        await savePlayerButton.click();

        await waitForSaveToComplete("Unknown");

        await browser.wait(() => tribeCardElement.click().then(() => true, () => false), 2000);

        expect(browser.getCurrentUrl()).toBe(`${hostName}/${tribe.id}/pairAssignments/current/`);

        browser.setLocation(`/${tribe.id}/player/${player1._id}`);
        expect(element(By.css('.player-card-header')).getText()).toBe('Unknown')
    });

    it('will show all players', function () {
        browser.setLocation(`/${tribe.id}/player/${player1._id}`);
        const playerElements = element.all(By.repeater('player in players'));
        expect(playerElements.getText()).toEqual(pluck('name', players));
    });
});

describe('The new player page', function () {

    const tribe = {
        _id: monk.id(),
        id: 'delete_me',
        name: 'Change Me'
    };

    const player1 = {_id: monk.id(), tribe: tribe.id, name: "player1"};
    const player2 = {_id: monk.id(), tribe: tribe.id, name: "player2"};
    const player3 = {_id: monk.id(), tribe: tribe.id, name: "player3"};
    const player4 = {_id: monk.id(), tribe: tribe.id, name: "player4"};
    const player5 = {_id: monk.id(), tribe: tribe.id, name: "player5"};
    const players = [
        player1,
        player2,
        player3,
        player4,
        player5
    ];

    beforeAll(function (done) {
        browser.get(`${hostName}/test-login?username=${e2eHelp.userEmail}&password="pw"`);

        tribeCollection.insert(tribe)
            .then(() => playersCollection.insert(players))
            .then(() => e2eHelp.authorizeUserForTribes([tribe.id]))
            .then(done, done.fail);
    });

    afterAll(function (done) {
        tribeCollection.remove({id: tribe.id}, false)
            .then(() => playersCollection.drop())
            .then(done, done.fail);
    });

    e2eHelp.afterEachAssertLogsAreEmpty();

    it('will show all players', function () {
        browser.setLocation(`/${tribe.id}/player/new`);
        const playerElements = element.all(By.repeater('player in players'));
        expect(playerElements.getText()).toEqual(pluck('name', players));
    });

    describe('when the tribe has call signs enabled', function () {

        const adjectiveTextInput = element(By.css('#adjective-input'));
        const nounTextInput = element(By.css('#noun-input'));

        beforeAll(async function () {
            const tribeClone: Tribe = clone(tribe);
            tribeClone.callSignsEnabled = true;
            await tribeCollection.update({_id: tribe._id}, tribeClone)
        });

        it(`will suggest call sign`, async function () {
            await browser.setLocation(`/${tribe.id}/player/new`);
            await browser.wait(() => adjectiveTextInput.isPresent(), 1000);
            let suggestedAdjective = await adjectiveTextInput.getAttribute('value');
            let suggestedNoun = await nounTextInput.getAttribute('value');
            await expect(suggestedAdjective).not.toBe('');
            await expect(suggestedNoun).not.toBe('');
        });
    });
});