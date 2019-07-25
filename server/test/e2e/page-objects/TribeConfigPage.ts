import setLocation from "../setLocation";
import {browser, By, element} from "protractor";
import {TribeConfigStyles} from "./Styles";

const tribeConfigElement = element(By.className(TribeConfigStyles.className));

async function waitForPage() {
    await browser.wait(() => tribeConfigElement.isPresent(), 2000);
}

export default {
    tribeConfigElement,
    async goTo(tribeId) {
        await setLocation(`/${tribeId}/edit/`);
        await waitForPage();

    },
    waitForPage
}