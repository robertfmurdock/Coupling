import * as Bluebird from 'bluebird';
import * as React from "react";
import {shallow} from 'enzyme';
import PairingRule from "../../common/PairingRule";
import ReactTribeConfig from "../app/components/tribe-config/ReactTribeConfig";
import waitFor from "./WaitFor";

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

describe('ReactTribeConfig', function () {

    const selectTribeDefer = defer();
    let selectedTribeId;

    beforeEach(function () {
        this.pathSetter = jasmine.createSpy('path');
        const selectedTribe = {
            name: 'Party tribe.',
            id: 'TotallyAwesome',
            _id: 'party'
        };
        this.coupling = {
            data: {
                selectedTribe: selectedTribe
            },
            saveTribe: function () {
            },
            selectTribe: function (tribeId) {
                selectedTribeId = tribeId;
                return selectTribeDefer.promise;
            }
        };
    });

    describe('will default an empty tribe', function () {

        beforeEach(function () {
            const tribe = {id: '1', name: '1'};

            this.wrapper = shallow(<ReactTribeConfig
                tribe={tribe}
                pathSetter={this.pathSetter}
                coupling={this.coupling}
            />);
        });

        it('to having standard pairing rule', function () {
            expect(this.wrapper.find('TribeForm').props().tribe.pairingRule)
                .toBe(PairingRule.LongestTime);
        });

        it('to having default badge name', function () {
            expect(this.wrapper.find('TribeForm').props().tribe.defaultBadgeName)
                .toBe('Default');
        });

        it('to having alternate badge name', function () {
            expect(this.wrapper.find('TribeForm').props().tribe.alternateBadgeName)
                .toBe('Alternate');
        });
    });

    describe('when pressing the save button ', function () {

        const saveTribeDefer = defer();
        let tribe;

        beforeEach(function () {
            this.saveTribeSpy = spyOn(this.coupling, 'saveTribe').and.returnValue(saveTribeDefer.promise);

            tribe = {};

            this.wrapper = shallow(<ReactTribeConfig
                tribe={tribe}
                pathSetter={this.pathSetter}
                coupling={this.coupling}
            />);
        });

        it('will use the Coupling service to save the tribe', async function () {
            saveTribeDefer.resolve();
            this.wrapper.find('#save-tribe-button').simulate('click');
            expect(this.saveTribeSpy).toHaveBeenCalled();
        });

        describe('when the save is complete', function () {

            it('will change the location to the current pair assignments', async function () {
                this.wrapper.find('#save-tribe-button').simulate('click');
                const newTribeId = 'expectedId';
                const expectedPath = '/tribes';
                expect(this.pathSetter).not.toHaveBeenCalledWith(expectedPath);

                const updatedTribe = {
                    _id: newTribeId
                };
                saveTribeDefer.resolve(updatedTribe);

                await waitFor(()=> this.pathSetter.calls.any(), 500);

                expect(this.pathSetter).toHaveBeenCalledWith(expectedPath);
            });
        });
    });
});
