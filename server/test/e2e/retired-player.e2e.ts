import {playersCollection, tribeCollection} from "./database";
import e2eHelp from "./e2e-help";
import {browser, By, element} from "protractor";
import * as monk from "monk";
import setLocation from "./setLocation";

const config = require("../../config/config");
const hostName = `http://${config.publicHost}:${config.port}`;

const deletedPlayerPage = {
    playerNameTextField: element(By.id('player-name'))
};

let playerConfigPage = element(By.css('.react-player-config'));

function waitForPlayerConfig() {
    return browser.wait(() => playerConfigPage.isPresent(), 2000);
}

describe('The retired player page ', function () {

    const tribe = {
        _id: monk.id(),
        id: 'delete_me',
        name: 'Change Me'
    };
    const player1 = {_id: monk.id(), tribe: tribe.id, name: "player1", isDeleted: true};
    const players = [player1];

    beforeAll(async function () {
        await tribeCollection.drop();
        await tribeCollection.insert(tribe);
        await e2eHelp.authorizeUserForTribes([tribe.id]);
        await browser.get(`${hostName}/test-login?username=${e2eHelp.userEmail}&password="pw"`);
        await playersCollection.drop();
        await playersCollection.insert(players);
    });

    afterAll(async function () {
        await tribeCollection.remove({id: tribe.id}, false);
        await playersCollection.drop();
    });

    e2eHelp.afterEachAssertLogsAreEmpty();

    it('will show the player data', async function () {
        await setLocation(`/${tribe.id}/retired-player/${player1._id}`);
        waitForPlayerConfig();

        expect(await deletedPlayerPage.playerNameTextField.getAttribute('value'))
            .toBe(player1.name)
    })

});