import {browser, element, by} from "protractor";
import e2eHelp from "./e2e-help";
import * as monk from "monk";

const config = require("../../config");
const hostName = 'http://' + config.publicHost + ':' + config.port;
const database = monk(config.tempMongoUrl);
const tribeCollection = database.get('tribes');

describe('The statistics page', function () {

    const tribe = {
        id: 'delete_me',
        name: 'Funkytown'
    };

    beforeAll(function (done) {
        browser.get(hostName + '/test-login?username=' + e2eHelp.userEmail + '&password="pw"');

        tribeCollection.remove({id: tribe.id})
            .then(() => tribeCollection.insert(tribe))
            .then(() => e2eHelp.authorizeUserForTribes([tribe.id]))
            .then(done, done.fail);
    });

    beforeEach(function () {
        browser.setLocation('/' + tribe.id + '/statistics/');
    });

    it('has a route which works', function () {
        const statisticsElement = element(by.css('statistics'));
        expect(statisticsElement.isPresent()).toBe(true);
    });

    it('has a tribe card with matching tribe', function () {
        const tribeCard = element(by.css('.tribe-name'));
        expect(tribeCard.getText()).toBe('Funkytown');
    });

});