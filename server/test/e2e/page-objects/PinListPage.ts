import {browser, By, element} from "protractor";
import {PinListStyles} from "./Styles";

const pinConfigPage = element(By.className(PinListStyles.className));

export default {

    pinConfigPage,

    async wait() {
        await browser.wait(() => pinConfigPage.isPresent(), 2000);
    },


}