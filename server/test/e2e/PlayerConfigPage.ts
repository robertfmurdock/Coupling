import setLocation from "./setLocation";
import {browser, By, element} from "protractor";

let playerConfigPage = element(By.css('.react-player-config'));

export default {

    async goToPlayerConfig(tribeId, playerId) {
        await setLocation(`/${tribeId}/player/${playerId}/`);
        await this.waitForPlayerConfig();
    },
    async goToNewPlayerConfig(tribeId) {
        await setLocation(`/${tribeId}/player/new/`);
        await this.waitForPlayerConfig();
    },
    async waitForPlayerConfig() {
        return await browser.wait(() => playerConfigPage.isPresent(), 2000);
    },

}