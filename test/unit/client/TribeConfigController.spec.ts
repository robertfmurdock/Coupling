import {TribeConfigController} from "../../../client/app/components/tribe-config/tribe-config";
import PairingRule from "../../../common/PairingRule";
import * as angular from "angular";
import * as _ from "underscore";

const defer = function () {
    const defer = {
        promise: null,
        resolve: null,
        reject: null
    };
    defer.promise = new Promise((resolve, reject) => {
        defer.resolve = resolve;
        defer.reject = reject;
    });
    return defer;
};

describe('TribeConfigController', function () {

    let Coupling, location, routeParams;
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
        Coupling = {
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
            this.controller = new TribeConfigController(location);
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
            tribe = {
                $save: jasmine.createSpy('save tribe spy').and.returnValue(saveTribeDefer.promise)
            };

            controller = new TribeConfigController(location);
            _.extend(controller, {tribe: tribe});
            controller.$onInit();
        });

        it('will use the Coupling service to save the tribe', function () {
            controller.clickSaveButton();
            expect(tribe.$save).toHaveBeenCalled();
        });

        describe('when the save is complete', function () {
            let callback;
            beforeEach(function () {
                controller.clickSaveButton();
                callback = tribe.$save.calls.argsFor(0)[1];
            });

            it('will change the location to the current pair assignments', function (done) {
                const newTribeId = 'expectedId';
                const expectedPath = '/tribes';
                expect(location.path).not.toHaveBeenCalledWith(expectedPath);

                const updatedTribe = {
                    _id: newTribeId
                };
                saveTribeDefer.resolve(updatedTribe);
                saveTribeDefer.promise.then(function () {
                    expect(location.path).toHaveBeenCalledWith(expectedPath);
                    done();
                })
            });
        });
    });
});
