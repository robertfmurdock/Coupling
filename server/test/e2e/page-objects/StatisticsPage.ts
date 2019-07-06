import setLocation from "../setLocation";
import {browser, By, element} from "protractor";
import * as styles from '../../../../client/app/components/statistics/styles.css'

const statisticsElement = element(By.className(styles.locals.statsPage));

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