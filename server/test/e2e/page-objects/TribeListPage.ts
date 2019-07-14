import {browser, By, element} from "protractor";
import setLocation from "../setLocation";
import {TribeCardStyles, TribeListStyles} from "./Styles";

const newTribeButton = element(By.className(TribeListStyles.newTribeButton));
const tribeCardHeaderLocator = By.className(TribeCardStyles.header);

export default {
    waitForTribeListPage: function () {
        browser.wait(() => newTribeButton.isPresent(), 2000)
    },
    getTribeElements: function () {
        return element.all(By.className(TribeCardStyles.className));
    },
    getTribeNameLabel: function (tribeElement) {
        return tribeElement.element(tribeCardHeaderLocator);
    },
    getNewTribeButton: function () {
        return newTribeButton;
    },
    async goTo() {
        await setLocation('/tribes/');
        await browser.wait(() =>
            newTribeButton.isPresent(), 2000);
    }
}