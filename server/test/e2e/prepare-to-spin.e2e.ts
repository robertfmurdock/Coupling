"use strict";
import {browser, By, element} from "protractor";
import * as monk from "monk";
import e2eHelp from "./e2e-help";
import * as pluck from 'ramda/src/pluck'
import setLocation from "./setLocation";
import {PairAssignmentsStyles, PlayerCardStyles, PlayerRosterStyles, PrepareSpinStyles} from "./page-objects/Styles";

const config = require("../../config/config");
const hostName = 'http://' + config.publicHost + ':' + config.port;
const database = monk.default(config.tempMongoUrl);
const tribeCollection = database.get('tribes');
const playersCollection = database.get('players');
const historyCollection = database.get('history');

const prepareToSpinPage = element(By.className(PrepareSpinStyles.className));

async function goToPrepare(tribe) {
    await setLocation(`/${tribe.id}/prepare/`);
    await browser.wait(() => prepareToSpinPage.isPresent(), 2000)
}

const pairAssignmentsPage = element(By.className(PairAssignmentsStyles.pairAssignments));

function waitForCurrentPairAssignmentPage() {
    browser.wait(() => pairAssignmentsPage.isPresent(), 1000);

}

describe('The prepare to spin page', function () {

    const spinButton = element(By.className(PrepareSpinStyles.spinButton));

    const tribe = {
        id: 'delete_me_prepare',
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

    beforeAll(async function () {
        await historyCollection.drop();
        await tribeCollection.insert(tribe);
        await e2eHelp.authorizeUserForTribes([tribe.id]);
        await playersCollection.drop();
        await playersCollection.insert(players);
        await browser.get(hostName + '/test-login?username=' + e2eHelp.userEmail + '&password="pw"');
    });

    afterAll(async function () {
        await setLocation(`/`);
        await tribeCollection.remove({id: tribe.id}, false)
    });

    e2eHelp.afterEachAssertLogsAreEmpty();

    beforeEach(async function () {
        await goToPrepare(tribe);
    });

    describe('with no history', function () {

        it('will show all the players ', function () {
            const playerElements = element.all(By.className(PlayerCardStyles.player));
            expect(playerElements.getText()).toEqual(pluck('name', players));
        });

        it('spinning with all players on will get all players back', function () {
            spinButton.click();
            waitForCurrentPairAssignmentPage();

            const pairs = element.all(By.css('.pair'));
            expect(pairs.count()).toEqual(3);
        });

        it('spinning with two players disabled will only yield one pair and then saving persists the pair', async function () {
            const playerElements = element.all(By.className(PlayerCardStyles.player));
            expect(playerElements.count()).toEqual(5);

            playerElements.get(0).element(By.className(PlayerCardStyles.playerIcon)).click();
            playerElements.get(2).element(By.className(PlayerCardStyles.playerIcon)).click();
            playerElements.get(3).element(By.className(PlayerCardStyles.playerIcon)).click();

            spinButton.click();
            waitForCurrentPairAssignmentPage();

            const pairs = element.all(By.css('.pair'));
            expect(pairs.count()).toEqual(1);

            const players = element.all(By.css(`.${PlayerRosterStyles.className} .${PlayerCardStyles.player}`));
            expect(players.count()).toEqual(3);

            const saveButton = element(By.id('save-button'));
            saveButton.click();
            waitForCurrentPairAssignmentPage();

            async function saveButtonIsDisplayed() {
                try {
                    return await saveButton.isDisplayed();
                } catch (e) {
                    return false;
                }
            }

            await browser.wait(async () => false === await saveButtonIsDisplayed(), 2000);
            waitForCurrentPairAssignmentPage();

            expect(pairs.count()).toEqual(1);
            expect(players.count()).toEqual(3);
        });
    });
});