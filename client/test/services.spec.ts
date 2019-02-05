import * as angular from "angular";
import "angular-resource";
import {Coupling} from "../app/services";
import axios from 'axios'
import * as Bluebird from 'bluebird'

const CouplingService = Coupling;

describe('Service: ', function () {

    describe('Coupling', function () {

        beforeEach(function () {
            angular.mock.module('coupling')
        });

        let Coupling;

        beforeEach(function () {
            Coupling = new CouplingService();
        });

        describe('get history', function () {
            it('calls back with history on success', async function () {
                const expectedHistory = [{
                    _id: 'one'
                }, {
                    _id: 'two'
                }];

                const getSpy = spyOn(axios, 'get')
                    .and.returnValue(Promise.resolve({data: expectedHistory}));

                const resultHistory = await Coupling.getHistory('tribo');

                expect(getSpy).toHaveBeenCalledWith('/api/tribo/history');

                expect(resultHistory.length).toBe(expectedHistory.length);
                expect(resultHistory[0]._id).toEqual(expectedHistory[0]._id);
                expect(resultHistory[1]._id).toEqual(expectedHistory[1]._id);
            });
        });

        describe('get tribes', function () {
            it('calls back with tribes on success', async function () {
                const expectedTribes = [{
                    _id: 'one'
                }, {
                    _id: 'two'
                }];

                const getSpy = spyOn(axios, 'get')
                    .and.returnValue(Promise.resolve({data: expectedTribes}));

                const resultTribes = await Coupling.getTribes();
                expect(getSpy).toHaveBeenCalledWith('/api/tribes');
                expect(angular.toJson(resultTribes)).toEqual(angular.toJson(expectedTribes));
            });

            it('shows error on failure', function (done) {
                const statusCode = 404;
                const url = '/api/tribes';
                const expectedData = 'nonsense';

                const getSpy = spyOn(axios, 'get')
                    .and.returnValue(Promise.reject(
                        {
                            response: {status: statusCode, data: expectedData}
                        }
                    ));

                let callCount = 0;

                Coupling.getTribes()
                    .then(function () {
                        callCount++;
                    })
                    .catch(function (error) {
                        expect(getSpy).toHaveBeenCalledWith(url);
                        expect(error).toBeDefined();
                        expect(error.response.status).toBe(statusCode);
                        expect(error.response.data).toBe(expectedData);
                        done();
                    });
            });
        });

        describe('save player', function () {
            it('will use axios', function (done) {

                const player = {
                    name: 'Navi'
                };

                const expectedUpdatedPlayer = {
                    name: 'Navi',
                    _id: '123'
                };

                const postSpy = spyOn(axios, 'post')
                    .and.returnValue(Promise.resolve({data: expectedUpdatedPlayer}));

                Coupling.savePlayer(player, 'tribo').then(function (updatedPlayer) {
                    expect(updatedPlayer).toEqual(expectedUpdatedPlayer);
                    expect(postSpy).toHaveBeenCalledWith('/api/tribo/players', player);
                    done();
                });
            })
        });

        describe('list all pins', function () {
            const tribeId = 'scruff';
            const url = '/api/' + tribeId + '/pins';

            it('will list all pins for a tribe', function (done) {
                const expectedPins = [{
                    stuff: 'maguff'
                }, {
                    stuff: 'mcduff'
                }];

                const getSpy = spyOn(axios, 'get')
                    .and.returnValue(Promise.resolve({data: expectedPins}));

                Bluebird.resolve(Coupling.getPins(tribeId))
                    .then(function (pins) {
                        expect(pins).toEqual(expectedPins);

                        expect(getSpy).toHaveBeenCalledWith(url);
                        done();
                    })
                    .catch(function (error) {
                        expect(error).toBeUndefined();
                    })
                    .finally(done);
            });

            it('shows error on failure', function (done) {
                const statusCode = 404;
                const expectedData = 'nonsense';
                const getSpy = spyOn(axios, 'get')
                    .and.returnValue(Promise.reject({
                        response: {status: statusCode, data: expectedData}
                    }));

                Coupling.getPins(tribeId).then(function () {
                    done.fail("This should not succeed.");
                }).catch(function (error) {
                    expect(error.response.status).toBe(statusCode);
                    expect(error.response.data).toBe(expectedData);
                    expect(getSpy).toHaveBeenCalledWith(url);
                    done();
                });
            });
        });
    });
});