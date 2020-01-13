import setLocation from "../setLocation";
import {browser, By, element} from "protractor";
import {PinConfigStyles} from "../page-objects/Styles";

let pinConfigPage = element(By.className(PinConfigStyles.className));

export default {

    async goToPinConfig(tribeId, pinId) {
        await setLocation(`/${tribeId}/pin/${pinId}/`);
        await this.wait();
    },
    async goToNewPinConfig(tribeId) {
        await setLocation(`/${tribeId}/pin/new/`);
        await this.wait();
    },

    async wait() {
        return await browser.wait(() => pinConfigPage.isPresent(), 2000);
    },

}