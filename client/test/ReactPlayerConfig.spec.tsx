import * as React from "react";
import {shallow} from 'enzyme';
import {Prompt} from 'react-router'
import Player from "../../common/Player";
import Badge from "../../common/Badge";
import merge from 'ramda/es/merge'
import ReactPlayerConfig from "../app/components/player-config/ReactPlayerConfig";

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

describe('ReactPlayerConfig', function () {

    const tribe = {
        name: 'Party tribe.',
        id: 'party',
        _id: 'hmm'
    };

    beforeEach(function () {
        this.pathSetter = jasmine.createSpy('path');

        this.coupling = {
            spin: jasmine.createSpy('spin'),
            savePlayer: jasmine.createSpy('save'),
            removePlayer: jasmine.createSpy('remove')
        };

    });

    it('when the given player has no badge, will use default badge', function () {
        const player = {_id: 'blarg'};
        const wrapper = shallow(<ReactPlayerConfig
            tribe={{
                name: 'Party tribe.',
                id: 'party',
                badgesEnabled: true
            }}
            player={player}
            players={[player]}
            pathSetter={this.pathSetter}
            coupling={this.coupling}
        />);
        expect(wrapper.find('PlayerConfigForm').shallow()
            .find('BadgeConfig').shallow()
            .find(`input[name='badge'][value=${Badge.Default}][checked=true]`).length)
            .toBe(1);
    });

    it('when the given player has alt badge, will not modify player', function () {
        const alternatePlayer: Player = {_id: '', badge: Badge.Alternate};
        const wrapper = shallow(<ReactPlayerConfig
            tribe={{
                name: 'Party tribe.',
                id: 'party',
                badgesEnabled: true
            }}
            player={alternatePlayer}
            players={[alternatePlayer]}
            pathSetter={this.pathSetter}
            coupling={this.coupling}
        />);
        expect(alternatePlayer.badge).toBe(Badge.Alternate);

        expect(wrapper.find('PlayerConfigForm').shallow()
            .find('BadgeConfig').shallow()
            .find(`input[name='badge'][value=${Badge.Alternate}][checked=true]`).length)
            .toBe(1);
    });

    it('can save player using Coupling service and then reloads', async function () {
        const player = {_id: 'blarg', badge: Badge.Alternate};
        let reloader = jasmine.createSpy();
        const wrapper = shallow(<ReactPlayerConfig
            tribe={{
                name: 'Party tribe.',
                id: 'party',
                badgesEnabled: true
            }}
            player={player}
            players={[player]}
            pathSetter={this.pathSetter}
            coupling={this.coupling}
            reloader={reloader}
        />);

        const saveDefer = defer();
        this.coupling.savePlayer.and.returnValue(saveDefer.promise);

        wrapper.find('PlayerConfigForm').shallow().find('input[name="name"]')
            .find('input[name="name"]')
            .simulate('change', {target: {name: 'name', value: 'nonsense'}, persist: () => undefined});

        wrapper.find('PlayerConfigForm').shallow().find('form')
            .simulate('submit');

        expect(this.coupling.savePlayer).toHaveBeenCalledWith(merge(player, {name: 'nonsense'}), tribe.id);

        saveDefer.resolve();

        await saveDefer.promise;
        expect(reloader).toHaveBeenCalled();
    });

    describe('clicking the delete button', function () {
        it('will remove and reroute to current pair assignments', async function () {
            const confirmSpy = spyOn(window, 'confirm');

            const deleteDefer = defer();
            this.coupling.removePlayer.and.returnValue(deleteDefer.promise);
            const player = {_id: 'blarg', badge: Badge.Alternate};
            const wrapper = shallow(<ReactPlayerConfig
                tribe={{
                    name: 'Party tribe.',
                    id: 'party',
                    badgesEnabled: true
                }}
                player={player}
                players={[player]}
                pathSetter={this.pathSetter}
                coupling={this.coupling}
            />);

            confirmSpy.and.returnValue(true);

            wrapper.find('PlayerConfigForm').shallow().find('.delete-button')
                .simulate('click');

            expect(this.coupling.removePlayer).toHaveBeenCalled();
            const argsFor = this.coupling.removePlayer.calls.argsFor(0);
            expect(argsFor[0]).toEqual(player);

            expect(this.pathSetter).not.toHaveBeenCalledWith(`/${tribe.id}/pairAssignments/current/`);
            deleteDefer.resolve();
            await deleteDefer.promise;
            expect(this.pathSetter).toHaveBeenCalledWith(`/${tribe.id}/pairAssignments/current/`);
        });

        it('will do nothing when not confirmed', function () {
            const confirmSpy = spyOn(window, 'confirm');
            const player = {_id: 'blarg'};
            const wrapper = shallow(<ReactPlayerConfig
                tribe={{
                    name: 'Party tribe.',
                    id: 'party'
                }}
                player={player}
                players={[player]}
                pathSetter={this.pathSetter}
                coupling={this.coupling}
            />);

            confirmSpy.and.returnValue(false);
            wrapper.find('PlayerConfigForm').shallow().find('.delete-button')
                .simulate('click');

            expect(this.coupling.removePlayer).not.toHaveBeenCalled();
            expect(this.pathSetter).not.toHaveBeenCalledWith(`/${tribe.id}/pairAssignments/current`);
        });
    });

    describe('on location change', function () {
        const player = {_id: 'blarg', badge: Badge.Alternate};

        beforeEach(function () {
            this.wrapper = shallow(<ReactPlayerConfig
                tribe={{
                    name: 'Party tribe.',
                    id: 'party'
                }}
                player={player}
                players={[player]}
                pathSetter={this.pathSetter}
                coupling={this.coupling}
            />);
        });

        it('when the player is changed, it will prompt the user to save', function () {
            this.wrapper.find('PlayerConfigForm').shallow().find('input[name="name"]')
                .simulate('change', {target: {name: 'name', value: 'differentName'}, persist: () => undefined});

            this.wrapper.update();

            expect(this.wrapper.find(Prompt).props().when)
                .toBe(true);
        });

        it('when the player is unchanged, it will not prompt the user to save', function () {
            expect(this.wrapper.find(Prompt).props().when)
                .toBe(false);
        });
    });
});
