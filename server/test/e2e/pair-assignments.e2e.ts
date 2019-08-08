"use strict";
import {browser, By, element} from "protractor";
import * as monk from "monk";
import PairAssignmentDocument from "../../../common/PairAssignmentDocument";
import e2eHelp from "./e2e-help";
import ApiGuy from "./apiGuy";
import setLocation from "./setLocation";
import {
    PairAssignmentsStyles,
    PlayerCardStyles,
    PlayerRosterStyles,
    TribeBrowserStyles,
    TribeCardStyles
} from "./page-objects/Styles";

const config = require("../../config/config");
const hostName = 'http://' + config.publicHost + ':' + config.port;
const database = monk.default(config.tempMongoUrl);
const tribeCollection = database.get('tribes');
const playersCollection = database.get('players');
const historyCollection = database.get('history');

const pluck = require('ramda/src/pluck');

const tribeCardHeaderElement = element(By.className(TribeCardStyles.header));
const pairAssignmentsPage = element(By.className(PairAssignmentsStyles.pairAssignments));

const unpairedPlayerElements = element.all(By.css(`.${PlayerRosterStyles.className} .${PlayerCardStyles.player}`));

function waitForCurrentPairAssignmentPage() {
    browser.wait(() => pairAssignmentsPage.isPresent(), 1000);
}

describe('The current pair assignments', function () {

    const tribe = {
        id: 'delete_me',
        name: 'Funkytown'
    };

    const player1 = {
        _id: monk.id(),
        tribe: tribe.id,
        name: "player1",
        callSignAdjective: 'nimble',
        callSignNoun: 'thimble'
    };
    const player2 = {
        _id: monk.id(),
        tribe: tribe.id,
        name: "player2",
        callSignAdjective: 'nimble',
        callSignNoun: 'thimble'
    };
    const player3 = {
        _id: monk.id(),
        tribe: tribe.id,
        name: "player3",
        callSignAdjective: 'nimble',
        callSignNoun: 'thimble'
    };
    const player4 = {
        _id: monk.id(),
        tribe: tribe.id,
        name: "player4",
        callSignAdjective: 'nimble',
        callSignNoun: 'thimble'
    };
    const player5 = {
        _id: monk.id(),
        tribe: tribe.id,
        name: "player5",
        callSignAdjective: 'nimble',
        callSignNoun: 'thimble'
    };
    const players = [
        player1,
        player2,
        player3,
        player4,
        player5
    ];

    beforeAll(async function () {
        await tribeCollection.insert(tribe);
        await e2eHelp.authorizeUserForTribes([tribe.id]);
        await playersCollection.drop();
        await playersCollection.insert(players);
        await browser.get(`${hostName}/test-login?username=${e2eHelp.userEmail}&password="pw"`);

        this.apiGuy = await ApiGuy.new(e2eHelp.userEmail)
    });

    afterAll(async function () {
        await tribeCollection.remove({id: tribe.id}, false);
    });

    e2eHelp.afterEachAssertLogsAreEmpty();


    it('shows the tribe', async function () {
        await setLocation(`/${tribe.id}/pairAssignments/current/`);
        waitForCurrentPairAssignmentPage();

        expect(tribeCardHeaderElement.getText()).toEqual(tribe.name);
    });

    it('will let you add players', async function () {
        await setLocation(`/${tribe.id}/pairAssignments/current/`);
        waitForCurrentPairAssignmentPage();

        element(By.className(PlayerRosterStyles.addPlayerButton)).click();
        expect(browser.getCurrentUrl()).toEqual(`${hostName}/${tribe.id}/player/new/`);
    });

    it('will let you edit an existing player', async function () {
        await setLocation(`/${tribe.id}/pairAssignments/current/`);
        waitForCurrentPairAssignmentPage();

        unpairedPlayerElements
            .first().element(By.className(PlayerCardStyles.header))
            .click();
        expect(browser.getCurrentUrl()).toEqual(`${hostName}/${tribe.id}/player/${player1._id}/`);
    });

    it('will let you view history', async function () {
        await setLocation('/' + tribe.id + '/pairAssignments/current/');
        waitForCurrentPairAssignmentPage();
        element(By.id('view-history-button')).click();
        expect(browser.getCurrentUrl()).toEqual(`${hostName}/${tribe.id}/history/`);
    });

    it('will let you prepare new pairs', async function () {
        await setLocation('/' + tribe.id + '/pairAssignments/current/');
        waitForCurrentPairAssignmentPage();
        element(By.id('new-pairs-button')).click();
        expect(browser.getCurrentUrl()).toEqual(`${hostName}/${tribe.id}/prepare/`);
    });

    it('will let you go to the stats page', async function () {
        await setLocation('/' + tribe.id + '/pairAssignments/current/');
        waitForCurrentPairAssignmentPage();
        element(By.className(TribeBrowserStyles.statisticsButton)).click();
        expect(browser.getCurrentUrl()).toEqual(`${hostName}/${tribe.id}/statistics`);
    });

    it('will let you see retired players', async function () {
        await setLocation('/' + tribe.id + '/pairAssignments/current/');
        waitForCurrentPairAssignmentPage();
        element(By.id('retired-players-button')).click();
        expect(browser.getCurrentUrl()).toEqual(`${hostName}/${tribe.id}/players/retired`);
    });

    describe('when there is no current set of pairs', function () {
        beforeAll(function (done) {
            historyCollection.drop()
                .then(done, done.fail);
        });
        it('will display all the existing players in the player roster', async function () {
            await setLocation('/' + tribe.id + '/pairAssignments/current/');
            waitForCurrentPairAssignmentPage();

            expect(unpairedPlayerElements.getText()).toEqual(pluck('name', players));
        });
    });

    describe('when there is a current set of pairs', function () {
        const pairAssignmentDocument = new PairAssignmentDocument(
            new Date(2015, 5, 30),
            [[player1, player3], [player5]]
        );

        beforeAll(async function () {
            await this.apiGuy.postPairAssignmentSet(tribe.id, pairAssignmentDocument);
            await browser.refresh();
        });

        beforeEach(async function () {
            await setLocation(`/${tribe.id}/pairAssignments/current/`);
            waitForCurrentPairAssignmentPage();
        });

        it('the most recent pairs are shown', function () {
            const pairElements = element.all(By.css('.pair'));
            const firstPair = pairElements.get(0).all(By.className(PlayerCardStyles.player));
            expect(firstPair.getText()).toEqual(pluck('name', [player1, player3]));
            const secondPair = pairElements.get(1).all(By.className(PlayerCardStyles.player));
            expect(secondPair.getText()).toEqual(pluck('name', [player5]));
        });

        it('only players that are not in the most recent pairs are displayed', function () {
            expect(unpairedPlayerElements.getText()).toEqual(pluck('name', [player2, player4]));
        });

        describe('and the tribe has toggled call signs off', function () {

            it('will not show the call sign for the pairs', async function () {
                await this.apiGuy.postTribe({
                    id: 'delete_me',
                    name: 'Funkytown',
                    callSignsEnabled: false
                });

                await setLocation(`/${tribe.id}/pairAssignments/current/`);
                waitForCurrentPairAssignmentPage();
                await browser.refresh();

                const callSigns = element.all(By.className('call-sign'));
                expect(await callSigns.count()).toBe(0)
            });

        });

        describe('and the tribe has toggled call signs on', function () {

            it('will show the call sign for the pairs', async function () {
                await this.apiGuy.postTribe({
                    id: 'delete_me',
                    name: 'Funkytown',
                    callSignsEnabled: true
                });

                await setLocation(`/${tribe.id}/pairAssignments/current/`);
                waitForCurrentPairAssignmentPage();
                await browser.refresh();
                waitForCurrentPairAssignmentPage();

                const pairElements = element.all(By.repeater('pair in pairAssignments.pairAssignments.pairs'));

                await pairElements.each(async function (elementFinder) {
                    const callSign = elementFinder.element(By.className('call-sign'));

                    expect(await callSign.isDisplayed()).toBe(true);
                    const callSignText = await callSign.getText();
                    expect(callSignText.split(' ').length).toBe(2);
                });
            });

        });
    });

});