"use strict";
import * as monk from 'monk'
import * as supertest from "supertest";

let config = require('../../config/config');

let server = 'http://localhost:' + config.port;
let agent = supertest.agent(server);
let tribeId = 'endpointTest';
let path = '/api/' + tribeId + '/pins';
let badTribePath = '/api/does-not-exist/pins';

let database = monk.default(config.tempMongoUrl);
let pinCollection = database.get('pins');

let clean = function (object) {
    return JSON.parse(JSON.stringify(object));
};

describe(path, function () {

    beforeEach(function (done) {
        agent.get('/test-login?username="name"&password="pw"')
            .expect(302)
            .then(done, done.fail);
    });

    describe("GET", function () {
        let expectedPins = [
            {_id: monk.id(), name: '1', tribe: tribeId},
            {_id: monk.id(), name: '2', tribe: tribeId},
            {_id: monk.id(), name: '3', tribe: tribeId}
        ];

        beforeEach(function (done) {
            pinCollection.remove({tribe: tribeId})
                .then(function () {
                    return pinCollection.insert(expectedPins)
                })
                .then(done, done.fail);
        });

        afterEach(function (done) {
            pinCollection.remove({tribe: tribeId})
                .then(done, done.fail);
        });

        it('will return all available pins on tribe.', function (done) {
            agent.get(path)
                .expect(200)
                .expect('Content-Type', /json/)
                .then(function (response) {
                    expect(clean(response.body)).toEqual(clean(expectedPins));
                })
                .then(done, done.fail);
        });

        it('will return error when tribe is not available.', async function () {
            await agent.get(badTribePath)
                .expect(404)
        });
    });

    describe("POST", function () {
        let resultPins = [
            {_id: monk.id().toString(), tribe: tribeId, name: '1'},
            {_id: monk.id().toString(), tribe: tribeId, name: '2'},
            {_id: monk.id().toString(), tribe: tribeId, name: '3'}
        ];

        beforeEach(function (done) {
            pinCollection.drop()
                .then(function () {
                    return pinCollection.insert(resultPins);
                })
                .then(done, done.fail);
        });

        afterEach(function (done) {
            pinCollection.drop()
                .then(done, done.fail);
        });

        it("will add pin to tribe", async function () {
            const newPin = {_id: monk.id().toString(), tribe: tribeId, name: 'lol'};
            const httpPost = agent.post(path);
            const response = await httpPost.send(newPin)
                .expect('Content-Type', /json/)
                .expect(200);

            const expectedPins = resultPins.concat(newPin);
            expect(clean(response.body)).toEqual(clean(newPin));

            const result = await agent.get(path);
            expect(clean(result.body)).toEqual(clean(expectedPins));
        });
    });
    describe("DELETE", function () {
        let resultPins = [
            {_id: monk.id(), name: '1', tribe: tribeId},
            {_id: monk.id(), name: '2', tribe: tribeId},
            {_id: monk.id(), name: '3', tribe: tribeId}
        ];

        beforeEach(function (done) {
            pinCollection.insert(resultPins)
                .then(done, done.fail);
        });

        afterEach(function (done) {
            pinCollection.remove({tribe: tribeId})
                .then(done, done.fail);
        });

        it('will no longer display the deleted pin', async function () {
            const deleteResponse = await agent.delete(path + "/" + resultPins[1]._id)
                .expect('Content-Type', /json/)
                .expect(200);
            expect(deleteResponse.body).toEqual(true);

            const getResponse = await agent.get(path)
                .expect(200)
                .expect('Content-Type', /json/);
            expect(clean(getResponse.body)).toEqual(clean([resultPins[0], resultPins[2]]));
        });

        it('will fail when pin does not exist', function (done) {
            let httpDelete = agent.delete(path + "/" + monk.id());
            httpDelete
                .expect('Content-Type', /json/)
                .expect(404)
                .then(function (response) {
                    expect(response.body).toEqual({message: 'Failed to remove the pin because it did not exist.'});
                })
                .then(done, done.fail);
        });
    });
});