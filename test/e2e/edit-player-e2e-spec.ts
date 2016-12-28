"use strict";
import {browser, element, By} from "protractor";
import * as _ from "underscore";
import * as monk from "monk";
import e2eHelp from "./e2e-help";

const config = require("../../config");
const hostName = 'http://' + config.publicHost + ':' + config.port;
const database = monk(config.tempMongoUrl);
const tribeCollection = database.get('tribes');
const playersCollection = database.get('players');


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
        element(By.css('.tribe')).click();
        expect(browser.getCurrentUrl()).toBe(`${hostName}/${tribe.id}/pairAssignments/current/`);
    });

    it('should get error on leaving when name is changed.', function (done) {
        browser.setLocation(`/${tribe.id}/player/${player1._id}`);
        expect(browser.getCurrentUrl()).toBe(`${hostName}/${tribe.id}/player/${player1._id}/`);
        element(By.id('player-name')).clear();
        element(By.id('player-name')).sendKeys('completely different name');
        element(By.css('.tribe img')).click();
        browser.wait(() =>
                browser.switchTo().alert()
                    .then(() => true, () => false)
            , 5000);

        browser.switchTo().alert()
            .then(function (alertDialog) {
                expect(alertDialog.getText()).toEqual('You have unsaved data. Would you like to save before you leave?');
                alertDialog.dismiss();
            })
            .then(done, done.fail);
    });

    it('should not get alert on leaving when name is changed after save.', function () {
        browser.setLocation('/' + tribe.id + '/player/' + player1._id);
        const playerNameTextField = element(By.id('player-name'));
        playerNameTextField.clear();
        playerNameTextField.sendKeys('completely different name');

        element(By.id('save-player-button')).click();
        element(By.css('.tribe')).click();
        expect(browser.getCurrentUrl()).toBe(hostName + '/' + tribe.id + '/pairAssignments/current/');

        browser.setLocation('/' + tribe.id + '/player/' + player1._id);
        expect(element(By.id('player-name')).getAttribute('value')).toBe('completely different name')
    });

    it('will show all players', function () {
        browser.setLocation('/' + tribe.id + '/player/' + player1._id);
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
        browser.get(hostName + '/test-login?username=' + e2eHelp.userEmail + '&password="pw"');

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
        browser.setLocation('/' + tribe.id + '/player/new');
        const playerElements = element.all(By.repeater('player in players'));
        expect(playerElements.getText()).toEqual(_.pluck(players, 'name'));
    });

});