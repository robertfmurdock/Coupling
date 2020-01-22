import PinConfigPage from "./page-objects/PinConfigPage"
import TestLogin from "./TestLogin";
import {tribeCollection} from "./database";
import e2eHelp from "./e2e-help";
import * as monk from "monk";
import {browser, By} from "protractor";
import ApiGuy from "./apiGuy";
import {IObjectID} from "monk";

describe('Pin', function () {

    const tribe = {
        _id: monk.id(),
        id: 'delete_me',
        name: 'Change Me'
    };

    beforeAll(async function () {
        await tribeCollection.drop();
        await tribeCollection.insert(tribe);
        await e2eHelp.authorizeUserForTribes([tribe.id]);
    });

    beforeAll(async function () {
        await TestLogin.login();
    });

    e2eHelp.afterEachAssertLogsAreEmpty();

    describe('when the pin is new', function () {

        beforeEach(async function () {
            await PinConfigPage.goToNewPinConfig(tribe.id)
        });

        it('and the add button is pressed, the pin is saved.', async function () {
            await PinConfigPage.nameTextField.clear();

            const newPinName = `Excellent pin name${monk.id()}`;
            await PinConfigPage.nameTextField.sendKeys(newPinName);
            await PinConfigPage.saveButton.click();
            await PinConfigPage.wait();

            await browser.wait(() => PinConfigPage.pinBag.isPresent());

            const pinNameElements = await PinConfigPage.pinBag.all(By.className("pin-name"));

            const pinNames = await Promise.all(
                // @ts-ignore
                pinNameElements.map(async it => await it.getText())
            );
            expect(pinNames).toContain(newPinName);
        })

    });

    describe('when the pin exists', function () {

        let pin: { icon: string; name: string; _id: IObjectID };

        beforeEach(async function () {
            pin = {_id: monk.id(), icon: "smile", name: "happy test pin"};
            const apiGuy = await ApiGuy.new(e2eHelp.userEmail);
            await apiGuy.postPin(tribe.id, pin);
            await PinConfigPage.goToPinConfig(tribe.id, pin._id)
        });

        it('will see all the pin attributes', async function () {
            expect(PinConfigPage.nameTextField.getAttribute('value'))
                .toBe(pin.name);
            expect(PinConfigPage.iconTextField.getAttribute('value'))
                .toBe(pin.icon);
        })

    })

});