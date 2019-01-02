import {browser} from "protractor";
import e2eHelp from "./e2e-help";

const config = require("../../server/config/config");
const hostName = 'http://' + config.publicHost + ':' + config.port;

describe('Pins', function () {

    xdescribe('On the add pin page', function () {

        it('will add a new pin when the add button is pressed.', function () {
            browser.get(`${hostName}/test-login?username=${e2eHelp.userEmail}&password="pw"`);
            const tribe = {id: 'lol'};
            browser.setLocation(`/${tribe.id}/player/new`);
        })


    })

});