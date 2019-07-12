import Pair from "../../common/Pair";
import {mount, shallow} from 'enzyme';
import * as React from "react";
import {ReactPairAssignments} from "../app/components/pair-assignments/ReactPairAssignments";
import {path} from "d3-path";
import {Player} from "../../common";
import PlayerRoster from "../app/components/player-roster/ReactPlayerRoster";

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

describe('ReactPairAssignments', function () {

    let Coupling;

    const selectedTribe = {
        name: 'Party tribe.',
        id: 'party'
    };

    it('will provide all of the players that are not in the current pairs', function () {
        const currentPairs: Pair[] = [
            [
                {name: 'tom', _id: '0'},
                {name: 'jerry', _id: 'z'}
            ], [
                {name: 'fellow', _id: '3'},
                {name: 'guy', _id: '2'}
            ]
        ];
        const players = [
            {name: 'rigby', _id: '1'},
            {name: 'guy', _id: '2'},
            {name: 'fellow', _id: '3'},
            {name: 'nerd', _id: '4'},
            {name: 'pantsmaster', _id: '5'}];


        const pairAssignments = {pairs: currentPairs, date: ''};

        const wrapper = shallow(<ReactPairAssignments
            isNew={false}
            pairAssignments={pairAssignments}
            tribe={selectedTribe}
            players={players}
            coupling={undefined}
            pathSetter={() => undefined}
        />);

        let rosterWrapper = wrapper.find(PlayerRoster);

        expect(rosterWrapper.props().players).toEqual([
            {name: 'rigby', _id: '1'},
            {name: 'nerd', _id: '4'},
            {name: 'pantsmaster', _id: '5'}
        ]);
    });

    it('will show no pair assignments when there is no history', function () {
        const tribeId = 'numbers';
        const players = [
            {name: 'rigby', _id: '1', tribe: tribeId},
            {name: 'guy', _id: '2', tribe: tribeId},
            {name: 'fellow', _id: '3', tribe: tribeId},
            {name: 'nerd', _id: '4', tribe: tribeId},
            {name: 'pantsmaster', _id: '5', tribe: tribeId}];

        const wrapper = shallow(<ReactPairAssignments
            isNew={false}
            pairAssignments={undefined}
            tribe={selectedTribe}
            players={players}
            coupling={Coupling}
            pathSetter={() => undefined}
        />);
        let rosterWrapper = wrapper.find(PlayerRoster);

        expect(rosterWrapper.props().players).toEqual(players);
    });

    const spinDefer = defer();

    const players = [{
        _id: 'h8',
        tribe: '1'
    }, {
        _id: '3r',
        tribe: '1'
    }, {
        _id: '8d3',
        tribe: '1'
    }];

    let pathSetter;

    beforeEach(function () {
        pathSetter = jasmine.createSpy('path');

        Coupling = {
            data: {},
            spin: jasmine.createSpy('spin'),
            saveCurrentPairAssignments: jasmine.createSpy('save')
        };
        Coupling.spin.and.returnValue(spinDefer.promise);
    });

    it('save will use Coupling service to save and then will redirect to the current pair assignments page', async function () {
        const wrapper = mount(<ReactPairAssignments
            isNew={true}
            pairAssignments={undefined}
            tribe={selectedTribe}
            players={players}
            coupling={Coupling}
            pathSetter={pathSetter}
        />);

        expect(Coupling.saveCurrentPairAssignments).not.toHaveBeenCalled();

        const successPromise = Promise.resolve('Complete');
        Coupling.saveCurrentPairAssignments.and.returnValue(successPromise);

        wrapper.find('#save-button').simulate('click');

        expect(Coupling.saveCurrentPairAssignments).toHaveBeenCalled();
        await successPromise;
        expect(pathSetter).toHaveBeenCalledWith(`/${selectedTribe.id}/pairAssignments/current/`);
    });

    it('onDrop will take two players and swap their places', function () {
        const player1: Player = {
            _id: '1',
            name: '1',
        };

        const player2: Player = {
            _id: '2',
            name: '2',
        };
        const player3: Player = {
            _id: '3',
            name: '3',
        };
        const player4: Player = {
            _id: '4',
            name: '4',
        };

        const pairAssignments = {
            pairs: [
                [player1, player2] as Pair,
                [player3, player4] as Pair
            ],
            date: ''
        };

        const wrapper = shallow(<ReactPairAssignments
            isNew={true}
            pairAssignments={pairAssignments}
            tribe={selectedTribe}
            players={players}
            coupling={Coupling}
            pathSetter={pathSetter}
        />);

        wrapper.find('AssignedPair').at(1).props()
            .swapCallback(player2._id, player3, pairAssignments.pairs[1]);
        wrapper.update();

        expect(wrapper.find('AssignedPair').at(0).props().pair)
            .toEqual([player1, player3]);
        expect(wrapper.find('AssignedPair').at(1).props().pair)
            .toEqual([player2, player4]);
    });

    it('onDrop will not swap players that are already paired', function () {
        const player1 = {
            _id: '1',
            name: '1',
            tribe: 'numbers'
        };
        const player2 = {
            _id: '2',
            name: '2',
            tribe: 'numbers'
        };
        const player3 = {
            _id: '3',
            name: '3',
            tribe: 'numbers'
        };
        const player4 = {
            _id: '4',
            name: '4',
            tribe: 'numbers'
        };

        const pairAssignments = {
            pairs: [
                [player1, player2] as Pair,
                [player3, player4] as Pair
            ],
            date: ''
        };

        const wrapper = shallow(<ReactPairAssignments
            isNew={true}
            pairAssignments={pairAssignments}
            tribe={selectedTribe}
            players={players}
            coupling={Coupling}
            pathSetter={pathSetter}
        />);

        wrapper.find('AssignedPair').at(1).props()
            .swapCallback(player4._id, player3, pairAssignments.pairs[1]);
        wrapper.update();

        expect(wrapper.find('AssignedPair').at(0).props().pair)
            .toEqual([player1, player2]);
        expect(wrapper.find('AssignedPair').at(1).props().pair)
            .toEqual([player3, player4]);
    });

    it('passes down tribe id to the server message', function () {
        const wrapper = shallow(<ReactPairAssignments
            isNew={true}
            pairAssignments={undefined}
            tribe={selectedTribe}
            players={players}
            coupling={Coupling}
            pathSetter={pathSetter}
        />);

        const serverMessage = wrapper.find('ReactServerMessage');

        expect(serverMessage.props().tribeId).toEqual(selectedTribe.id);
    });
});

