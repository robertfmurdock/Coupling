"use strict";
import {browser, element, By} from "protractor";
import * as monk from "monk";
import e2eHelp from "./e2e-help";
import * as _ from "underscore";

const config = require("../../config");
const hostName = 'http://' + config.publicHost + ':' + config.port;
const database = monk.default(config.tempMongoUrl);
const tribeCollection = database.get('tribes');
const playersCollection = database.get('players');

describe('The retired players page', function () {

    const tribe = {
        id: 'graveyard',
        name: 'Arkham Asylum'
    };

    const player1 = {_id: monk.id(), tribe: tribe.id, name: "player1", isDeleted: true};
    const player2 = {_id: monk.id(), tribe: tribe.id, name: "player2", isDeleted: true};
    const player3 = {_id: monk.id(), tribe: tribe.id, name: "player3", isDeleted: null};
    const player4 = {_id: monk.id(), tribe: tribe.id, name: "player4", isDeleted: true};
    const players = [
        player1,
        player2,
        player3,
        player4
    ];

    const retiredPlayers = [
        player1,
        player2,
        player4
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

    beforeEach(function () {
        browser.setLocation(`/${tribe.id}/players/retired`);
    });

    it('shows the retired players', function () {
        const playerElements = element.all(By.repeater('player in retiredPlayers'));
        expect(playerElements.getText()).toEqual(_.pluck(retiredPlayers, 'name'));
    });

});