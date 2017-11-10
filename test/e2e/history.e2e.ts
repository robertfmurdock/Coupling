"use strict";
import {browser, element, by} from "protractor";
import PairAssignmentDocument from "../../common/PairAssignmentDocument";
import * as monk from "monk";
import * as supertest from "supertest";
import * as Promise from "bluebird";
import e2eHelp from "./e2e-help";

const config = require('../../config');
const hostName = `http://${config.publicHost}:${config.port}`;
const agent = supertest.agent(hostName);
const database = monk.default(config.tempMongoUrl);
const historyCollection = database.get('history');
const tribeCollection = database.get('tribe');

const loginSupertest = function () {
    return agent.get('/test-login?username="name"&password="pw"')
        .expect(302);
};

const postTribe = function (tribe) {
    return agent.post('/api/tribes')
        .send(tribe)
        .expect(200);
};
const postPairAssignmentSet = function (tribeId, pairAssignmentSet) {
    return agent.post('/api/' + tribeId + '/history')
        .send(pairAssignmentSet)
        .expect(200);
};

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

    it('shows recent pairings', function () {
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

        browser.wait(() => loginSupertest()
                .then(() => postTribe(tribe))
                .then(() => Promise.all([
                    e2eHelp.authorizeUserForTribes([tribe.id]),
                    postPairAssignmentSet(tribe.id, pairAssignmentSet1),
                    postPairAssignmentSet(tribe.id, pairAssignmentSet2)
                ]))
                .then(() => true)
            , 1000);

        browser.waitForAngular();

        browser.setLocation(`/${tribe.id}/history`);

        const pairAssignmentSetElements = element.all(by.className('pair-assignments'));
        expect(pairAssignmentSetElements.count()).toBe(2);
    });

});