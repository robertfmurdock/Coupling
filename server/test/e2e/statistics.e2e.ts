import {browser, element, By} from "protractor";
import e2eHelp from "./e2e-help";
import * as monk from "monk";
import setLocation from "./setLocation";
import TestLogin from "./TestLogin";
import StatisticsPage from "./page-objects/StatisticsPage";
import {PairReportTableStyles, TeamStatisticsStyles, TribeCardStyles} from "./page-objects/Styles";

const config = require("../../config/config");
const database = monk.default(config.tempMongoUrl);
const tribeCollection = database.get('tribes');
const playerCollection = database.get('players');

describe('The statistics page', function () {

    const tribe = {
        id: 'delete_me',
        name: 'Funkytown'
    };

    const players = [
        {_id: monk.id(), tribe: tribe.id},
        {_id: monk.id(), tribe: tribe.id},
        {_id: monk.id(), tribe: tribe.id},
        {_id: monk.id(), tribe: tribe.id},
        {_id: monk.id(), tribe: tribe.id},
        {_id: monk.id(), tribe: tribe.id},
    ];

    const tribeCardHeaderElement = element(By.className(TribeCardStyles.header));

    beforeAll(async function () {
        await TestLogin.login();
        await tribeCollection.remove({id: tribe.id});
        await tribeCollection.insert(tribe);
        await playerCollection.remove({tribe: tribe.id});
        await playerCollection.insert(players);
        await e2eHelp.authorizeUserForTribes([tribe.id]);
    });

    beforeAll(async function () {
        await StatisticsPage.goTo(tribe.id);
    });

    it('has a route which works', function () {
        expect(StatisticsPage.statisticsElement.isPresent()).toBe(true);
    });

    it('has a tribe card with matching tribe', function () {
        expect(tribeCardHeaderElement.getText()).toBe('Funkytown');
    });

    it('has the number of spins until full rotation', function () {
        const rotationNumberElement = element(By.className(TeamStatisticsStyles.rotationNumber));
        expect(rotationNumberElement.getText()).toBe('5');
    });

    it('has the pair reports', function () {
        const pairReports = element.all(By.className(PairReportTableStyles.pairReport));
        expect(pairReports.count()).toBe(15);
    });

});