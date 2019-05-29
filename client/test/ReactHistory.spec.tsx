import {configure, mount} from 'enzyme';
import * as React from 'react'
import * as Adapter from 'enzyme-adapter-react-16';
import PairAssignmentSet from '../../common/PairAssignmentSet';
import ReactHistory from "../app/components/history/ReactHistory";
import waitFor from "./WaitFor";

configure({adapter: new Adapter()});

describe('ReactHistory Spec', function () {

    it('will delete pair set when remove is called and confirmed', async function () {
        const $remove = jasmine.createSpy('removeSpy');
        const reload = jasmine.createSpy('reload');
        const coupling = {removeAssignments: $remove};
        const tribe = {id: 'me', name: 'you'};
        const history: PairAssignmentSet[] = [{
            pairs: [],
            date: ''
        }];

        const wrapper = mount(<ReactHistory
            tribe={tribe} history={history} coupling={coupling} reload={reload}
            pathSetter={() => undefined}/>);
        spyOn(window, 'confirm').and.returnValue(true);
        wrapper.find('.delete-button').simulate('click');

        await waitFor(() => $remove.calls.count() > 0, 20);
        expect($remove).toHaveBeenCalled();
        expect(reload).toHaveBeenCalled();
    });

    it('will not delete pair set when remove is called and not confirmed', async function () {
        const $remove = jasmine.createSpy('removeSpy');
        const tribe = {id: 'me', name: 'you'};
        const history: PairAssignmentSet[] = [{
            pairs: [],
            date: ''
        }];
        const coupling = {
            removeAssignments: $remove
        };
        const wrapper = mount(<ReactHistory
            tribe={tribe} history={history} coupling={coupling}
            pathSetter={() => undefined} reload={() => undefined}/>);

        spyOn(window, 'confirm').and.returnValue(false);
        wrapper.find('.delete-button').simulate('click');
        expect($remove).not.toHaveBeenCalled();
    });

});