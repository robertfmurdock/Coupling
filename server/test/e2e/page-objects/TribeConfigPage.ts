import setLocation from "../setLocation";
import {browser, By, element} from "protractor";

const tribeConfigElement = element(By.className("tribe-config"));

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