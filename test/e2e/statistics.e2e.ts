import {browser, element, by} from "protractor";
import e2eHelp from "./e2e-help";
const config = require("../../config");
const hostName = 'http://' + config.publicHost + ':' + config.port;

describe('The statistics page', function () {

    const tribe = {
        id: 'delete_me',
        name: 'Funkytown'
    };

    beforeAll(function (done) {
        browser.get(hostName + '/test-login?username=' + e2eHelp.userEmail + '&password="pw"');

        e2eHelp.authorizeUserForTribes([tribe.id])
            .then(done, done.fail);
    });

    beforeEach(function () {
        browser.setLocation('/' + tribe.id + '/statistics/');
    });

    it('has a route which works', function () {
        const statisticsElement = element(by.css('statistics'));
        expect(statisticsElement.isPresent()).toBe(true);
    });

});