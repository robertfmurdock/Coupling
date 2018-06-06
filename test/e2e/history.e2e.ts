"use strict";
import {browser, By, by, element} from "protractor";
import PairAssignmentDocument from "../../common/PairAssignmentDocument";
import * as monk from "monk";
import e2eHelp from "./e2e-help";
import ApiGuy from "./apiGuy";

const config = require('../../config');
const hostName = `http://${config.publicHost}:${config.port}`;
const database = monk.default(config.tempMongoUrl);
const historyCollection = database.get('history');
const tribeCollection = database.get('tribe');


describe('The history page', function () {

    const tribe = {id: 'excellent', name: 'make-by-test'};

    beforeAll(function () {
        browser.wait(() => Promise.all([
            tribeCollection.drop(),
            historyCollection.drop()
        ]), 1000);

        browser.get(`${hostName}/test-login?username=${e2eHelp.userEmail}&password="pw"`);
    });

    afterAll(function (done) {
        historyCollection.drop()
            .then(done, done.fail);
    });

    e2eHelp.afterEachAssertLogsAreEmpty();

    describe('with two assignments', function () {

        beforeAll(async function () {
            const pairAssignmentSet1 = new PairAssignmentDocument(new Date().toISOString(), [[
                {
                    name: 'Ollie',
                    tribe: tribe.id,
                    _id: monk.id()
                },
                {
                    name: 'Speedy',
                    tribe: tribe.id,
                    _id: monk.id()
                }
            ]], tribe.id);
            const pairAssignmentSet2 = new PairAssignmentDocument(new Date().toISOString(), [[
                {name: 'Arthur', tribe: tribe.id, _id: monk.id()},
                {name: 'Garth', tribe: tribe.id, _id: monk.id()}
            ]], tribe.id);

            const apiGuy = await ApiGuy.new();
            await apiGuy.postTribe(tribe);
            await Promise.all([
                e2eHelp.authorizeUserForTribes([tribe.id]),
                apiGuy.postPairAssignmentSet(tribe.id, pairAssignmentSet1),
                apiGuy.postPairAssignmentSet(tribe.id, pairAssignmentSet2)
            ]);

            await browser.waitForAngular();

            await browser.setLocation(`/${tribe.id}/history`);
        });

        it('shows recent pairings', async function () {
            const pairAssignmentSetElements = element.all(by.className('pair-assignments'));
            expect(pairAssignmentSetElements.count()).toBe(2);
        });


        it('can be deleted', async function () {
            const pairAssignmentSetElements = element.all(by.className('pair-assignments'));
            const deleteButton = pairAssignmentSetElements.get(0).element(By.css('.delete-button'));

            deleteButton.click();
            const alert = await (browser.switchTo().alert() as Promise<any>);

            alert.accept();

            await browser.waitForAngular();


            expect(pairAssignmentSetElements.count()).toBe(1);
        });


    });

});