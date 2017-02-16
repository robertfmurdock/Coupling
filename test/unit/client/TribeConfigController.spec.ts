import "angular";
import "angular-mocks";
import {TribeConfigController} from "../../../client/app/components/tribe-config/tribe-config";
import Tribe from "../../../common/Tribe";
import PairingRule from "../../../common/PairingRule";

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

    beforeEach(angular.mock.module('coupling.tribeConfig'));

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

    it('will default an empty tribe to having standard pairing rule', inject(function (_$controller_) {
        const tribe: Tribe = {id: '1', name: '1'};
        _$controller_('TribeConfigController', {$location: location}, {tribe: tribe});
        expect(tribe.pairingRule).toBe(PairingRule.LongestTime);
    }));

    describe('when pressing the save button ', function () {

        const saveTribeDefer = defer();
        let tribe;
        let controller;

        beforeEach(inject(function (_$controller_) {
            tribe = {
                $save: jasmine.createSpy('save tribe spy').and.returnValue(saveTribeDefer.promise)
            };

            controller = _$controller_('TribeConfigController', {$location: location}, {tribe: tribe});
        }));

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
