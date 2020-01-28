"use strict";
import {browser, By, element} from "protractor";
import * as monk from "monk";
import e2eHelp from "./e2e-help";
import * as pluck from 'ramda/src/pluck'
import setLocation from "./setLocation";
import {
    AssignedPairStyles,
    PairAssignmentsStyles, PinButtonStyles,
    PlayerCardStyles,
    PlayerRosterStyles,
    PrepareSpinStyles
} from "./page-objects/Styles";
import ApiGuy from "./apiGuy";

const config = require("../../config/config");
const hostName = `http://${config.publicHost}:${config.port}`;
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

    const tribe = {id: monk.id(), name: 'Funkytown'};

    const player1 = {_id: monk.id(), name: "player1"};
    const player2 = {_id: monk.id(), name: "player2"};
    const player3 = {_id: monk.id(), name: "player3"};
    const player4 = {_id: monk.id(), name: "player4"};
    const player5 = {_id: monk.id(), name: "player5"};
    const players = [
        player1,
        player2,
        player3,
        player4,
        player5
    ];

    const pin = {_id: monk.id(), name: 'e2e-pin'};

    beforeAll(async function () {
        const apiGuy = await ApiGuy.new(e2eHelp.userEmail);
        await apiGuy.postTribe(tribe);
        // await e2eHelp.authorizeUserForTribes([tribe.id]);

        await apiGuy.postPin(tribe.id, pin);

        for (const it of players) {
            await apiGuy.postPlayer(tribe.id, it)
        }

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

            const pairs = element.all(By.className(AssignedPairStyles.className));
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

            const pairs = element.all(By.className(AssignedPairStyles.className));
            expect(pairs.count()).toEqual(1);

            const players = element.all(By.css(`.${PlayerRosterStyles.className} .${PlayerCardStyles.player}`));
            expect(players.count()).toEqual(3);

            const saveButton = element(By.className(PairAssignmentsStyles.saveButton));
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

        it('spinning with pin enabled will include that pin in assignment', async function () {
            const selectedPinElements = element(By.className(PrepareSpinStyles.selectedPins))
                .all(By.className(PinButtonStyles.className));
            expect(selectedPinElements.count()).toEqual(1);

            spinButton.click();

            waitForCurrentPairAssignmentPage();

            const assignedPins = element.all(By.className(PinButtonStyles.className));

            expect(assignedPins.count()).toEqual(1);
        });

        it('spinning with pin disabled will exclude that pin from assignment', async function () {
            const selectedPinElements = element(By.className(PrepareSpinStyles.selectedPins))
                .all(By.className(PinButtonStyles.className));
            expect(selectedPinElements.count()).toEqual(1);

            selectedPinElements.click();
            spinButton.click();

            waitForCurrentPairAssignmentPage();

            const assignedPins = element.all(By.className(PinButtonStyles.className));

            expect(assignedPins.count()).toEqual(0);
        });
    });
});