import {TribeConfigController} from "../../../client/app/components/tribe-config/tribe-config";
import PairingRule from "../../../common/PairingRule";
import * as Bluebird from 'bluebird';
import * as angular from "angular";
import * as _ from "underscore";
import {Coupling} from "../../../client/app/services";

const defer = function () {
    const defer = {
        promise: null,
        resolve: null,
        reject: null
    };
    defer.promise = new Bluebird((resolve, reject) => {
        defer.resolve = resolve;
        defer.reject = reject;
    });
    return defer;
};

describe('TribeConfigController', function () {

    let coupling, location, routeParams;
    const selectTribeDefer = defer();
    let selectedTribeId;

    beforeEach(angular.mock.module('coupling'));

    beforeEach(function () {
        location = {
            path: jasmine.createSpy('path')
        };
        const selectedTribe = {
            name: 'Party tribe.',
            id: 'TotallyAwesome',
            _id: 'party'
        };
        coupling = {
            data: {
                selectedTribe: selectedTribe
            },
            selectTribe: function (tribeId) {
                selectedTribeId = tribeId;
                return selectTribeDefer.promise;
            }
        };
        routeParams = {
            tribeId: selectedTribe.id
        };
    });

    describe('will default an empty tribe', function () {

        beforeEach(function () {
            const tribe = {id: '1', name: '1'};
            this.controller = new TribeConfigController(location, coupling, {});
            this.controller.tribe = tribe;
            this.controller.$onInit();
        });

        it('to having standard pairing rule', function () {
            expect(this.controller.tribe.pairingRule).toBe(PairingRule.LongestTime);
        });

        it('to having default badge name', function () {
            expect(this.controller.tribe.defaultBadgeName).toBe('Default');
        });

        it('to having alternate badge name', function () {
            expect(this.controller.tribe.alternateBadgeName).toBe('Alternate');
        });
    });

    describe('when pressing the save button ', function () {

        const saveTribeDefer = defer();
        let tribe;
        let controller;

        beforeEach(function () {
            this.saveTribeSpy = spyOn(Coupling, 'saveTribe').and.returnValue(saveTribeDefer.promise);

            tribe = {};

            controller = new TribeConfigController(location, coupling, {
                $apply() {
                }
            });
            _.extend(controller, {tribe: tribe});
            controller.$onInit();
        });

        it('will use the Coupling service to save the tribe', async function () {
            saveTribeDefer.resolve();
            await controller.clickSaveButton();
            expect(this.saveTribeSpy).toHaveBeenCalled();
        });

        describe('when the save is complete', function () {

            it('will change the location to the current pair assignments', async function () {
                const saveClickPromise = controller.clickSaveButton();
                const newTribeId = 'expectedId';
                const expectedPath = '/tribes';
                expect(location.path).not.toHaveBeenCalledWith(expectedPath);

                const updatedTribe = {
                    _id: newTribeId
                };
                saveTribeDefer.resolve(updatedTribe);

                await saveClickPromise;
                expect(location.path).toHaveBeenCalledWith(expectedPath);
            });
        });
    });
});
