"use strict";
import {browser, By, element} from "protractor";
import * as _ from "underscore";
import {playersCollection, tribeCollection} from "./database";
import e2eHelp from "./e2e-help";
import Tribe from "../../common/Tribe";
import * as monk from "monk";

const config = require("../../config");
const hostName = 'http://' + config.publicHost + ':' + config.port;


const tribeCardStyles = require('../../client/app/components/tribe-card/styles.css');

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

    const tribeCardElement = element(By.className(tribeCardStyles.className));
    const savePlayerButton = element(By.id('save-player-button'));

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

    it('should not alert on leaving when nothing has changed.', function () {
        browser.setLocation(`/${tribe.id}/player/${player1._id}`);
        tribeCardElement.click();
        expect(browser.getCurrentUrl()).toBe(`${hostName}/${tribe.id}/pairAssignments/current/`);
    });

    describe('when the tribe does not have badging enabled', function () {

        beforeEach(function (done) {
            const tribeClone: Tribe = _.clone(tribe);
            tribeClone.badgesEnabled = false;
            tribeCollection.update({_id: tribe._id}, tribeClone)
                .then(done, done.fail);
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

        beforeEach(function (done) {
            const tribeClone: Tribe = _.clone(tribe);
            tribeClone.badgesEnabled = true;
            tribeClone.defaultBadgeName = "Badge 1";
            tribeClone.alternateBadgeName = "Badge 2";
            tribeCollection.update({_id: tribe._id}, tribeClone)
                .then(done, done.fail);
        });

        it('should show the badge selector', function () {
            browser.setLocation(`/${tribe.id}/player/${player1._id}`);
            expect(defaultBadgeRadio.isDisplayed()).toEqual(true);
            expect(element(By.css('label[for=default-badge-radio]')).getText()).toBe('Badge 1');

            expect(altBadgeRadio.isDisplayed()).toEqual(true);
            expect(element(By.css('label[for=alt-badge-radio]')).getText()).toBe('Badge 2');
        });

        it('the player default badge should be selected', function () {
            expect(defaultBadgeRadio.getAttribute('checked')).toBe('true');
        });

        it('should remember badge selection', function () {
            browser.setLocation(`/${tribe.id}/player/${player1._id}`);
            altBadgeRadio.click();
            savePlayerButton.click();
            browser.setLocation(`/${tribe.id}/player/${player1._id}`);
            expect(altBadgeRadio.getAttribute('checked')).toBe('true');
        });

    });


    it('should get error on leaving when name is changed.', function (done) {
        browser.setLocation(`/${tribe.id}/player/${player1._id}`);
        expect(browser.getCurrentUrl()).toBe(`${hostName}/${tribe.id}/player/${player1._id}/`);
        element(By.id('player-name')).clear();
        element(By.id('player-name')).sendKeys('completely different name');
        element(By.css(`.${tribeCardStyles.className} img`)).click();
        browser.wait(() =>
                browser.switchTo().alert()
                    .then(() => true, () => false)
            , 5000);

        browser.switchTo().alert()
            .then(function (alertDialog) {
                expect(alertDialog.getText())
                    .toEqual('You have unsaved data. Would you like to save before you leave?');
                alertDialog.dismiss();
            })
            .then(done, done.fail);
    });

    it('should not get alert on leaving when name is changed after save.', function () {
        browser.setLocation(`/${tribe.id}/player/${player1._id}`);
        const playerNameTextField = element(By.id('player-name'));
        playerNameTextField.clear();
        playerNameTextField.sendKeys('completely different name');

        savePlayerButton.click();
        tribeCardElement.click();
        expect(browser.getCurrentUrl()).toBe(`${hostName}/${tribe.id}/pairAssignments/current/`);

        browser.setLocation(`/${tribe.id}/player/${player1._id}`);
        expect(element(By.id('player-name')).getAttribute('value')).toBe('completely different name')
    });

    it('saving with no name will show as a default name.', function () {
        browser.setLocation(`/${tribe.id}/player/${player1._id}`);
        const playerNameTextField = element(By.id('player-name'));
        playerNameTextField.clear();
        savePlayerButton.click();
        tribeCardElement.click();
        expect(browser.getCurrentUrl()).toBe(`${hostName}/${tribe.id}/pairAssignments/current/`);

        browser.setLocation(`/${tribe.id}/player/${player1._id}`);
        expect(element(By.css('[ng-model="playerCard.player.name"]')).getText()).toBe('Unknown')
    });

    it('will show all players', function () {
        browser.setLocation(`/${tribe.id}/player/${player1._id}`);
        const playerElements = element.all(By.repeater('player in players'));
        expect(playerElements.getText()).toEqual(_.pluck(players, 'name'));
    });
});

describe('The new player page', function () {

    const tribe = {
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
        expect(playerElements.getText()).toEqual(_.pluck(players, 'name'));
    });

});