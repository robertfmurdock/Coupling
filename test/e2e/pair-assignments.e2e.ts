"use strict";
import {browser, element, By} from "protractor";
import * as monk from "monk";
import * as _ from "underscore";
import PairAssignmentDocument from "../../common/PairAssignmentDocument";
import e2eHelp from "./e2e-help";

const config = require("../../config");
const hostName = 'http://' + config.publicHost + ':' + config.port;
const database = monk.default(config.tempMongoUrl);
const tribeCollection = database.get('tribes');
const playersCollection = database.get('players');
const historyCollection = database.get('history');

describe('The current pair assignments', function () {

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
        browser.get(`${hostName}/test-login?username=${e2eHelp.userEmail}&password="pw"`);
        browser.wait(() =>
                tribeCollection.insert(tribe)
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

    const tribeCardStyles = require('../../client/app/components/tribe-card/styles.css');
    const tribeCardHeaderElement = element(By.className(tribeCardStyles.header));

    it('shows the tribe', function () {
        browser.setLocation(`/${tribe.id}/pairAssignments/current/`);

        expect(tribeCardHeaderElement.getText()).toEqual(tribe.name);
    });

    it('will let you add players', function () {
        browser.setLocation(`/${tribe.id}/pairAssignments/current/`);
        element(By.id('add-player-button')).click();
        expect(browser.getCurrentUrl()).toEqual(`${hostName}/${tribe.id}/player/new/`);
    });

    it('will let you edit an existing player', function () {
        browser.setLocation(`/${tribe.id}/pairAssignments/current/`);

        const playerCardStyles = require('../../client/app/components/player-card/styles.css');

        element.all(By.repeater('player in players'))
            .first().element(By.className(playerCardStyles.header))
            .click();
        expect(browser.getCurrentUrl()).toEqual(`${hostName}/${tribe.id}/player/${player1._id}/`);
    });

    it('will let you view history', function () {
        browser.setLocation('/' + tribe.id + '/pairAssignments/current/');
        element(By.id('view-history-button')).click();
        expect(browser.getCurrentUrl()).toEqual(`${hostName}/${tribe.id}/history/`);
    });

    it('will let you prepare new pairs', function () {
        browser.setLocation('/' + tribe.id + '/pairAssignments/current/');
        element(By.id('new-pairs-button')).click();
        expect(browser.getCurrentUrl()).toEqual(`${hostName}/${tribe.id}/prepare/`);
    });

    it('will let you go to the stats page', function () {
        browser.setLocation('/' + tribe.id + '/pairAssignments/current/');
        const styles = require('../../client/app/components/tribebrowser/styles.css');
        element(By.className(styles.statisticsButton)).click();
        expect(browser.getCurrentUrl()).toEqual(`${hostName}/${tribe.id}/statistics`);
    });

    it('will let you see retired players', function () {
        browser.setLocation('/' + tribe.id + '/pairAssignments/current/');
        element(By.id('retired-players-button')).click();
        expect(browser.getCurrentUrl()).toEqual(`${hostName}/${tribe.id}/players/retired`);
    });

    describe('when there is no current set of pairs', function () {
        beforeAll(function (done) {
            historyCollection.drop()
                .then(done, done.fail);
        });
        it('will display all the existing players in the player roster', function () {
            browser.setLocation('/' + tribe.id + '/pairAssignments/current/');
            const playerElements = element.all(By.repeater('player in players'));
            expect(playerElements.getText()).toEqual(_.pluck(players, 'name'));
        });
    });

    describe('when there is a current set of pairs', function () {
        const pairAssignmentDocument = new PairAssignmentDocument(
            new Date(2015, 5, 30),
            [[player1, player3], [player5]],
            tribe.id
        );

        beforeAll(function () {
            browser.wait(()=> historyCollection.insert(pairAssignmentDocument), 1000);
            browser.refresh();
        });

        beforeEach(function () {
            browser.setLocation('/' + tribe.id + '/pairAssignments/current/');
        });

        it('the most recent pairs are shown', function () {
            const pairElements = element.all(By.repeater('pair in pairAssignments.pairAssignments.pairs'));
            const firstPair = pairElements.get(0).all(By.repeater('player in pair'));
            expect(firstPair.getText()).toEqual(_.pluck([player1, player3], 'name'));
            const secondPair = pairElements.get(1).all(By.repeater('player in pair'));
            expect(secondPair.getText()).toEqual(_.pluck([player5], 'name'));
        });

        it('only players that are not in the most recent pairs are displayed', function () {
            const remainingPlayerElements = element.all(By.repeater('player in players'));
            expect(remainingPlayerElements.getText()).toEqual(_.pluck([player2, player4], 'name'));
        });
    });

});