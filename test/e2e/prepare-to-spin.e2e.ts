"use strict";
import {browser, By, element} from "protractor";
import * as monk from "monk";
import * as _ from "underscore";
import e2eHelp from "./e2e-help";

const config = require("../../config");
const hostName = 'http://' + config.publicHost + ':' + config.port;
const database = monk.default(config.tempMongoUrl);
const tribeCollection = database.get('tribes');
const playersCollection = database.get('players');
const historyCollection = database.get('history');

describe('The prepare to spin page', function () {

    const prepareStyles = require('../../client/app/components/prepare/styles.css');
    const spinButton = element(By.className(prepareStyles.spinButton));

    const tribe = {
        id: 'delete_me',
        name: 'Funkytown'
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

    beforeAll(function () {
        browser.get(hostName + '/test-login?username=' + e2eHelp.userEmail + '&password="pw"');

        browser.wait(() =>
                historyCollection.drop()
                    .then(() => tribeCollection.insert(tribe))
                    .then(() => e2eHelp.authorizeUserForTribes([tribe.id]))
                    .then(() => playersCollection.drop())
                    .then(() => playersCollection.insert(players))
            , 1000);
        browser.waitForAngular();
    });

    afterAll(function (done) {
        tribeCollection.remove({id: tribe.id}, false)
            .then(done, done.fail);
    });

    e2eHelp.afterEachAssertLogsAreEmpty();

    beforeEach(function () {
        browser.setLocation(`/${tribe.id}/prepare/`);
    });

    describe('with no history', function () {

        it('will show all the players ', function () {
            const playerElements = element.all(By.repeater('selectable in prepare.selectablePlayers'));
            expect(playerElements.getText()).toEqual(_.pluck(players, 'name'));
        });

        it('spinning with all players on will get all players back', function () {
            spinButton.click();

            const pairs = element.all(By.repeater('pair in pairAssignments.pairAssignments.pairs'));
            expect(pairs.count()).toEqual(3);
        });

        it('spinning with two players disabled will only yield one pair and then saving persists the pair', function () {
            const playerElements = element.all(By.repeater('selectable in prepare.selectablePlayers'));
            expect(playerElements.count()).toEqual(5);

            playerElements.get(0).element(By.css('.player-icon')).click();
            playerElements.get(2).element(By.css('.player-icon')).click();
            playerElements.get(3).element(By.css('.player-icon')).click();

            spinButton.click();

            const pairs = element.all(By.repeater('pair in pairAssignments.pairAssignments.pairs'));
            expect(pairs.count()).toEqual(1);
            const players = element.all(By.repeater('player in players'));
            expect(players.count()).toEqual(3);

            element(By.id('save-button')).click();

            expect(pairs.count()).toEqual(1);
            expect(players.count()).toEqual(3);
        });
    });
});