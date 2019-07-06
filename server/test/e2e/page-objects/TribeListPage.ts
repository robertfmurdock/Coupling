import {browser, By, element} from "protractor";
import setLocation from "../setLocation";

const tribeCardHeaderLocator = By.className("tribe-card-header");
const newTribeButton = element(By.id('new-tribe-button'));

export default {
    waitForTribeListPage: function () {
        browser.wait(() => newTribeButton.isPresent(), 2000)
    },
    getTribeElements: function () {
        return element.all(By.className('tribe-card'));
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