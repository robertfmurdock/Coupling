"use strict";
import CouplingDataService from "../../server/lib/CouplingDataService";
import * as Promise from 'bluebird'
import * as monk from 'monk'
import * as supertest from "supertest";

let config = require('../../server/config/config');

let server = 'http://localhost:' + config.port;
let agent = supertest.agent(server);
let dataService = new CouplingDataService(config.tempMongoUrl);
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
            {_id: monk.id(), tribe: tribeId},
            {_id: monk.id(), tribe: tribeId},
            {_id: monk.id(), tribe: tribeId}
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

        it('will return error when tribe is not available.', function (done) {
            agent.get(badTribePath)
                .expect('Content-Type', /json/)
                .then(function (response) {
                    expect(response.body).toEqual([]);
                })
                .then(done, done.fail);
        });
    });

    describe("POST", function () {
        let resultPins = [
            {_id: monk.id(), tribe: tribeId},
            {_id: monk.id(), tribe: tribeId},
            {_id: monk.id(), tribe: tribeId}
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

        it("will add pin to tribe", function (done) {
            let newPin = {_id: monk.id(), tribe: tribeId};
            let httpPost = agent.post(path);
            httpPost.send(newPin)
                .expect('Content-Type', /json/)
                .expect(200)
                .then(function (response) {
                    let expectedPins = resultPins.concat(newPin);
                    expect(clean(response.body)).toEqual(clean(newPin));

                    return Promise.props({
                        expectedPins: expectedPins,
                        results: dataService.requestPins(tribeId)
                    });
                })
                .then(function (props: any) {
                    expect(props.results).toEqual(props.expectedPins);
                })
                .then(done, done.fail);
        });
    });
    describe("DELETE", function () {
        let resultPins = [
            {_id: monk.id(), tribe: tribeId},
            {_id: monk.id(), tribe: tribeId},
            {_id: monk.id(), tribe: tribeId}
        ];

        beforeEach(function (done) {
            pinCollection.insert(resultPins)
                .then(done, done.fail);
        });

        afterEach(function (done) {
            pinCollection.remove({tribe: tribeId})
                .then(done, done.fail);
        });

        it('will no longer display the deleted pin', function (done) {
            let httpDelete = agent.delete(path + "/" + resultPins[1]._id);
            httpDelete
                .expect('Content-Type', /json/)
                .expect(200)
                .then(function (response) {
                    expect(response.body).toEqual({});

                    return agent.get(path)
                        .expect(200)
                        .expect('Content-Type', /json/)
                })
                .then(function (response) {
                    expect(clean(response.body)).toEqual(clean([resultPins[0], resultPins[2]]));
                })
                .then(done, done.fail);
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