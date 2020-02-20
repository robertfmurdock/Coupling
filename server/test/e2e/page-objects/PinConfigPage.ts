import setLocation from "../setLocation";
import {browser, By, element} from "protractor";
import {PinConfigEditorStyles, PinConfigStyles} from "./Styles";

const pinConfigPage = element(By.className(PinConfigStyles.className));
const saveButton = element(By.className(PinConfigEditorStyles.saveButton));
const deleteButton = element(By.className(PinConfigEditorStyles.deleteButton));
const pinBag = element(By.className(PinConfigStyles.pinBag));
const nameTextField = element(By.id('pin-name'));
const iconTextField = element(By.id('pin-icon'));

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
        await browser.wait(() => pinConfigPage.isPresent(), 2000);
    },
    saveButton,
    nameTextField,
    iconTextField,
    deleteButton,
    pinBag

}