import {browser, By, element} from "protractor";
import * as styles from '../../../../client/build/resources/main/com/zegreatrob/coupling/client/Welcome.css'

const config = require("../../../config/config");
const hostName = `http://${config.publicHost}:${config.port}`;

const enterButton = element(By.className("enter-button"));
const googleButton = element(By.className("google-login"));
const microsoftButton = element(By.className("ms-login"));

const thing = element(By.className(styles.className));

async function goTo() {
    await browser.get(`${hostName}/welcome`);
    await waitForPage();
}

async function waitForPage() {
    await browser.wait(() => thing.isPresent(), 2000);
}


export default {
    enterButton,
    googleButton,
    microsoftButton,
    goTo,
    waitForPage
}