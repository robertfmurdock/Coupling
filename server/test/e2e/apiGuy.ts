import * as supertest from "supertest";

const config = require('../../config/config');
const hostName = `http://${config.publicHost}:${config.port}`;
const agent = supertest.agent(hostName);

const defaultUsername = '"name"';
const defaultPassword = '"pw"';

export default class ApiGuy {

    static async new(username = defaultUsername, password = defaultPassword) {
        const apiGuy = new ApiGuy();
        await apiGuy.loginSupertest(username, password);
        return apiGuy;
    }

    private async loginSupertest(username, password) {
        return await agent.get('/test-login?username=' + username + '&password=' + password)
            .expect(302);
    };

    async postTribe(tribe) {
        await agent.post('/api/tribes')
            .send(tribe)
            .expect(200);
        return this;
    };

    async postPairAssignmentSet(tribeId, pairAssignmentSet) {
        await agent.post('/api/' + tribeId + '/history')
            .send(pairAssignmentSet)
            .expect(200);
        return this;
    };

}