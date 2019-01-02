import * as supertest from "supertest";

const config = require('../../server/config/config');
const hostName = `http://${config.publicHost}:${config.port}`;
const agent = supertest.agent(hostName);

export default class ApiGuy {

    static async new() {
        const apiGuy = new ApiGuy();
        await apiGuy.loginSupertest();
        return apiGuy;
    }

    private async loginSupertest() {
        return await agent.get('/test-login?username="name"&password="pw"')
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