import {browser} from "protractor";
import e2eHelp from "./e2e-help";
import TribeListPage from "./page-objects/TribeListPage";

const config = require("../../config/config");
const hostName = `http://${config.publicHost}:${config.port}`;

export default {
    async login() {
        await browser.get(`${hostName}/test-login?username=${e2eHelp.userEmail}&password="pw"`);
        await TribeListPage.waitForTribeListPage();
    }
}