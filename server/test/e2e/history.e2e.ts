"use strict";
import {browser, By, by, element} from "protractor";
import PairAssignmentDocument from "../../../common/PairAssignmentDocument";
import * as monk from "monk";
import e2eHelp from "./e2e-help";
import ApiGuy from "./apiGuy";
import setLocation from "./setLocation";
import TestLogin from "./TestLogin";
import {HistoryStyles} from "./page-objects/Styles";

const config = require('../../config/config');
const database = monk.default(config.tempMongoUrl);
const historyCollection = database.get('history');
const tribeCollection = database.get('tribe');


describe('The history page', function () {

    const tribe = {id: 'excellent', name: 'make-by-test'};

    beforeAll(async function () {
        await Promise.all([
            tribeCollection.drop(),
            historyCollection.drop()
        ]);

        await TestLogin.login();
    });

    afterAll(async function () {
        await historyCollection.drop();
    });

    e2eHelp.afterEachAssertLogsAreEmpty();

    describe('with two assignments', function () {

        beforeAll(async function () {
            const pairAssignmentSet1 = new PairAssignmentDocument(new Date().toISOString(), [[
                {
                    name: 'Ollie',
                    _id: monk.id()
                },
                {
                    name: 'Speedy',
                    _id: monk.id()
                }
            ]]);
            const pairAssignmentSet2 = new PairAssignmentDocument(new Date().toISOString(), [[
                {name: 'Arthur', _id: monk.id()},
                {name: 'Garth', _id: monk.id()}
            ]]);

            const apiGuy = await ApiGuy.new();
            await apiGuy.postTribe(tribe);
            await Promise.all([
                e2eHelp.authorizeUserForTribes([tribe.id]),
                apiGuy.postPairAssignmentSet(tribe.id, pairAssignmentSet1),
                apiGuy.postPairAssignmentSet(tribe.id, pairAssignmentSet2)
            ]);

            await setLocation(`/${tribe.id}/history`);
            await browser.wait(() => element(By.className(HistoryStyles.historyView)).isPresent(), 2000)
        });

        it('shows recent pairings', async function () {
            const pairAssignmentSetElements = element.all(by.className('pair-assignments'));
            expect(pairAssignmentSetElements.count()).toBe(2);
        });


        it('can be deleted', async function () {
            const pairAssignmentSetElements = element.all(by.className('pair-assignments'));
            const deleteButton = pairAssignmentSetElements.get(0).element(By.className(HistoryStyles.deleteButton));

            deleteButton.click();
            const alert = await browser.switchTo().alert();

            alert.accept();

            await browser.wait(async () => await pairAssignmentSetElements.count() === 1, 2000);

            expect(pairAssignmentSetElements.count()).toBe(1);
        });

    });

});