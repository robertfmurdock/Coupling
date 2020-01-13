import PinConfigPage from "./page-objects/PinConfigPage"
import TestLogin from "./TestLogin";
import {tribeCollection} from "./database";
import e2eHelp from "./e2e-help";
import * as monk from "monk";

const config = require("../../config/config");

describe('Pin', function () {

    const tribe = {
        _id: monk.id(),
        id: 'delete_me',
        name: 'Change Me'
    };

    beforeAll(async function () {
        await tribeCollection.drop();
        await tribeCollection.insert(tribe);
        await e2eHelp.authorizeUserForTribes([tribe.id]);
    });

    beforeAll(async function () {
        await TestLogin.login();
    });

    e2eHelp.afterEachAssertLogsAreEmpty();

    describe('when the pin is new', function () {

        beforeEach(async function() {
            await PinConfigPage.goToNewPinConfig(tribe.id)
        });

        xit('will add a new pin when the add button is pressed.', async function () {

        })

    })

});