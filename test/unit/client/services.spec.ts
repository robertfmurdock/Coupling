"use strict";

import * as angular from 'angular'
import "angular-resource";
import 'angular-mocks';
import {Coupling} from "../../../client/app/services";

const CouplingService = Coupling;

describe('Service: ', function () {

    describe('Coupling', function () {

        beforeEach(angular.mock.module('ngResource'));

        let httpBackend;
        let Coupling, q, rootScope;

        beforeEach(function () {
            inject(function ($httpBackend, $q, $rootScope, $http, $resource) {
                httpBackend = $httpBackend;
                q = $q;
                rootScope = $rootScope;
                Coupling = new CouplingService($http, $q, $resource);
            });
        });

        describe('get history', function () {
            it('calls back with history on success', function (done) {
                const expectedHistory = [{
                    _id: 'one'
                }, {
                    _id: 'two'
                }];
                httpBackend.whenGET('/api/tribo/history').respond(200, expectedHistory);

                Coupling.getHistory('tribo')
                    .then(function (resultHistory) {
                        expect(resultHistory.length).toBe(expectedHistory.length);
                        expect(resultHistory[0]._id).toEqual(expectedHistory[0]._id);
                        expect(resultHistory[1]._id).toEqual(expectedHistory[1]._id);
                        done();
                    })
                    .catch(function (error) {
                        expect(error).toBeUndefined();
                    })
                    .finally(done);
                httpBackend.flush();
            });
        });

        describe('get tribes', function () {
            it('calls back with tribes on success', function (done) {
                const expectedTribes = [{
                    _id: 'one'
                }, {
                    _id: 'two'
                }];

                httpBackend.whenGET('/api/tribes').respond(200, expectedTribes);

                Coupling.getTribes()
                    .then(function (resultTribes) {
                        expect(angular.toJson(resultTribes)).toEqual(angular.toJson(expectedTribes));
                        done();
                    }).catch(function (error) {
                    expect(error).toBeUndefined();
                }).finally(done);

                httpBackend.flush();
            });

            it('shows error on failure', function (done) {
                const statusCode = 404;
                const url = '/api/tribes';
                const expectedData = 'nonsense';
                httpBackend.whenGET(url).respond(statusCode, expectedData);
                let callCount = 0;

                Coupling.getTribes()
                    .then(function () {
                        callCount++;
                    }).catch(function (error) {
                    expect(error).toBeDefined();
                    expect(error.status).toBe(statusCode);
                    expect(error.data).toBe(expectedData);
                    done();
                });
                httpBackend.flush();
            });
        });

        describe('save player', function () {
            it('will use http service', function (done) {

                const player = {
                    name: 'Navi',
                    tribe: 'tribo'
                };

                const expectedUpdatedPlayer = {
                    name: 'Navi',
                    tribe: 'tribo',
                    _id: '123'
                };
                httpBackend.whenPOST('/api/tribo/players').respond(200, expectedUpdatedPlayer);

                Coupling.savePlayer(player).then(function (updatedPlayer) {
                    expect(updatedPlayer).toEqual(expectedUpdatedPlayer);
                    done();
                });

                httpBackend.flush();
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
                httpBackend.whenGET(url).respond(200, expectedPins);

                const pinsPromise = Coupling.getPins(tribeId);
                pinsPromise.then(function (pins) {
                    expect(pins).toEqual(expectedPins);
                    done();
                }).catch(function (error) {
                    expect(error).toBeUndefined();
                }).finally(done);
                httpBackend.flush();
            });

            it('shows error on failure', function (done) {
                const statusCode = 404;
                const expectedData = 'nonsense';
                httpBackend.whenGET(url).respond(statusCode, expectedData);
                Coupling.getPins(tribeId).then(function () {
                    done.fail("This should not succeed.");
                }).catch(function (error) {
                    expect(error.status).toBe(statusCode);
                    expect(error.data).toBe(expectedData);
                    done();
                });
                httpBackend.flush();
            });
        });
    });
});