import {browser, element, by, By} from "protractor";
import e2eHelp from "./e2e-help";
import * as monk from "monk";
import Player from "../../common/Player";

const config = require("../../server/config/config");
const hostName = 'http://' + config.publicHost + ':' + config.port;
const database = monk.default(config.tempMongoUrl);
const tribeCollection = database.get('tribes');
const playerCollection = database.get('players');

describe('The statistics page', function () {

    const tribe = {
        id: 'delete_me',
        name: 'Funkytown'
    };

    const players: Player[] = [
        {_id: monk.id(), tribe: tribe.id},
        {_id: monk.id(), tribe: tribe.id},
        {_id: monk.id(), tribe: tribe.id},
        {_id: monk.id(), tribe: tribe.id},
        {_id: monk.id(), tribe: tribe.id},
        {_id: monk.id(), tribe: tribe.id},
    ];

    const tribeCardHeaderElement = element(By.className("tribe-card-header"));

    beforeAll(function (done) {
        browser.get(hostName + '/test-login?username=' + e2eHelp.userEmail + '&password="pw"');

        tribeCollection.remove({id: tribe.id})
            .then(() => tribeCollection.insert(tribe))
            .then(() => playerCollection.remove({tribe: tribe.id}))
            .then(() => playerCollection.insert(players))
            .then(() => e2eHelp.authorizeUserForTribes([tribe.id]))
            .then(done, done.fail);
    });

    beforeAll(function () {
        browser.setLocation('/' + tribe.id + '/statistics/');
    });

    it('has a route which works', function () {
        const statisticsElement = element(By.css('statistics'));
        expect(statisticsElement.isPresent()).toBe(true);
    });

    it('has a tribe card with matching tribe', function () {
        expect(tribeCardHeaderElement.getText()).toBe('Funkytown');
    });

    it('has the number of spins until full rotation', function () {
        const rotationNumberElement = element(By.css('.rotation-number'));
        expect(rotationNumberElement.getText()).toBe('5');
    });

    it('has the pair reports', function () {
        const pairReports = element.all(By.css('[ng-repeat="report in self.statistics.pairReports"]'));
        expect(pairReports.count()).toBe(15);
    });

});