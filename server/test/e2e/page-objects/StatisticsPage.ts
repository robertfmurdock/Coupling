import setLocation from "../setLocation";
import {browser, By, element} from "protractor";
import {TribeStatisticsStyles} from "./Styles";

const statisticsElement = element(By.className(TribeStatisticsStyles.className));

async function waitForPage() {
    browser.wait(() => statisticsElement.isPresent(), 2000);
}

export default {
    statisticsElement,
    async goTo(tribeId: string) {
        await setLocation(`/${tribeId}/statistics/`);
        await waitForPage();
    },
    waitForPage
}