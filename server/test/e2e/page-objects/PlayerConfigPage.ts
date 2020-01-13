import setLocation from "../setLocation";
import {browser, By, element} from "protractor";
import {PlayerConfigStyles} from "./Styles";

let playerConfigPage = element(By.className(PlayerConfigStyles.className));

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