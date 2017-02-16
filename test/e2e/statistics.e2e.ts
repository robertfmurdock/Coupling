import {browser, element, by} from "protractor";
import e2eHelp from "./e2e-help";
const config = require("../../config");
const hostName = 'http://' + config.publicHost + ':' + config.port;

describe('The statistics page', function () {

    const tribe = {
        id: 'delete_me',
        name: 'Funkytown'
    };

    beforeAll(function () {
        browser.get(hostName + '/test-login?username=' + e2eHelp.userEmail + '&password="pw"');

        browser.wait(() => e2eHelp.authorizeUserForTribes([tribe.id]), 1000);
        browser.waitForAngular();
    });

    beforeEach(function () {
        browser.setLocation('/' + tribe.id + '/statistics/');
    });

    it('has a route which works', function () {
        const statisticsElement = element(by.css('statistics'));
        expect(statisticsElement.isPresent()).toBe(true);
    });

});