'use strict';
import * as supertest from "supertest";
import * as Bluebird from "bluebird";
import * as monk from "monk";
import * as pluck from 'ramda/src/pluck'
import * as find from 'ramda/src/find'

let config = require('../../config/config');
let server = 'http://localhost:' + config.port;

let path = '/api/tribes';
let host = supertest.agent(server);

function clean(object) {
    return JSON.parse(JSON.stringify(object));
}

let database = monk.default(config.tempMongoUrl);
let tribesCollection = database.get('tribes');
let playersCollection = database.get('players');
let usersCollection = monk.default(config.mongoUrl).get('users');

describe(path, function () {
    let userEmail = 'test@test.tes';

    beforeEach(function (done) {
        host.get('/test-login?username=' + userEmail + '&password=pw')
            .expect(302)
            .then(() => Bluebird.all([
                playersCollection.drop(),
                tribesCollection.drop()
            ]))
            .then(done, done.fail);
    });

    async function authorizeUserForTribes(authorizedTribes) {
        await usersCollection.remove({email: userEmail + "._temp"});
        await usersCollection.insert({email: userEmail + "._temp", tribes: authorizedTribes, timestamp: new Date()});
    }

    it('GET will return all available tribes.', async function () {
        const tribes = [
            {id: 'Uno', name: 'one'},
            {id: 'Dos', name: 'two'},
            {id: 'Tres', name: 'three'}
        ];

        await tribesCollection.insert(tribes);
        let authorizedTribes = pluck('id', tribes);
        await authorizeUserForTribes(authorizedTribes);
        const response = await host.get(path)
            .expect(200)
            .expect('Content-Type', /json/);

        expect(pluck('id', response.body))
            .toEqual(pluck('id', tribes));
        expect(pluck('names', response.body))
            .toEqual(pluck('names', tribes));
    });

    it('GET will return any tribe that has a player with the given email.', async function () {
        let tribe = {id: 'delete-me', name: 'tribe-from-endpoint-tests'};
        let playerId = monk.id();
        await Bluebird.all([
            tribesCollection.insert(tribe),
            playersCollection.insert({
                _id: playerId,
                name: 'delete-me',
                tribe: 'delete-me',
                email: userEmail + '._temp'
            }),
            authorizeUserForTribes([])
        ]);
        const response = await host.get(path)
            .expect(200)
            .expect('Content-Type', /json/);
        expect(pluck('id', response.body)).toEqual(['delete-me']);
        expect(pluck('name', response.body)).toEqual(['tribe-from-endpoint-tests']);

        await Bluebird.all([
            tribesCollection.remove({id: 'delete-me'}, false),
            playersCollection.remove({_id: playerId})
        ])
    });

    it('GET will not return the tribe if a player had that email but had it removed.', async function () {
        let tribe = {id: 'delete-me', name: 'tribe-from-endpoint-tests'};
        let playerId = monk.id();
        await Bluebird.all([
            host.post(path).send(tribe),
            host.post(path + '/players').send({
                _id: playerId,
                name: 'delete-me',
                tribe: 'delete-me',
                email: userEmail + '._temp'
            })
        ]);

        await authorizeUserForTribes([]);

        await host.post(path + '/players').send({
            _id: playerId,
            name: 'delete-me',
            tribe: 'delete-me',
            email: 'something else '
        });

        const response = await host.get(path)
            .expect(200)
            .expect('Content-Type', /json/);
        expect(clean(response.body)).toEqual(clean([]));

        await Bluebird.all([
            tribesCollection.remove({id: 'delete-me'}, false),
            playersCollection.remove({_id: playerId})
        ]);
    });

    it('GET will not return all available tribes when the user does not have explicit permission.', function (done) {
        authorizeUserForTribes([])
            .then(() => {
                return host.get(path)
                    .expect(200)
                    .expect('Content-Type', /json/)
            })
            .then(function (response) {
                expect(response.body).toEqual([]);
            })
            .then(done, done.fail);
    });

    describe('POST', function () {
        let newTribe = {
            name: 'TeamMadeByTest',
            id: 'deleteme',
            email: 'test@test.test',
            badgesEnabled: true,
            _id: monk.id()
        };

        it('will create a tribe and authorize it.', async function () {
            const firstResponse = await host.post(path)
                .send(newTribe)
                .expect(200)
                .expect('Content-Type', /json/);

            expect(JSON.stringify(firstResponse.body)).toEqual(JSON.stringify(newTribe));

            const secondResponse = await host.get(path)
                .expect(200)
                .expect('Content-Type', /json/);

            const result = find(function (element) {
                return element.name === 'TeamMadeByTest'
            }, secondResponse.body);

            expect(result).toBeDefined();
            expect(result.id).toBe('deleteme');
            expect(result.email).toBe('test@test.test');
            expect(result.badgesEnabled).toBe(true);
        });

        it('when the tribe already exists and you do not have permission, will fail', async function () {
            let tribe = {id: 'Something else', name: 'one'};
            await Bluebird.all([
                tribesCollection.insert(tribe),
                authorizeUserForTribes([])
            ]);
            await host.post(path)
                .send(tribe)
                .expect(400)
        });

        afterAll(function (done) {
            tribesCollection.remove({id: newTribe.id}, false)
                .then(done, done.fail);
        });
    });
});