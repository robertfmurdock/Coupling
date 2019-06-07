import {mount, shallow} from 'enzyme';
import "ng-fittext";
import "../app/components/components";
import {Randomizer} from "../../client/app/services";
import Player from "../../common/Player";
import ReactWelcomeView, {WelcomeSplash} from "../app/components/welcome/ReactWelcomeView";
import * as React from "react";
import * as Styles from '../app/components/welcome/styles.css'
import waitFor from "./WaitFor";

describe('ReactWelcomeView:', function () {

    function buildView(randomValue) {
        let randomizer = new Randomizer();
        spyOn(randomizer, 'next').and.returnValue(randomValue);
        return mount(<ReactWelcomeView randomizer={randomizer}/>)
    }

    it('does not show initially', function () {
        const wrapper = buildView(0);
        let rootDiv = wrapper.find(`.${Styles.className}`);
        expect(rootDiv.hasClass(Styles.hidden)).toBe(true);
    });

    it('will show after a zero timeout so that the animation works', async function () {
        const wrapper = buildView(0);
        await waitFor(() => {
            wrapper.update();
            return wrapper.find(`.${Styles.className}`).hasClass(Styles.hidden) === false;
        }, 100);
    });

    it('will choose return hobbits when it rolls a zero.', function () {
        let randomValue = 0;
        let randomizer = new Randomizer();
        spyOn(randomizer, 'next').and.returnValue(randomValue);
        const wrapper = shallow(<WelcomeSplash randomizer={randomizer} hiddenTag={""}/>);

        const expectedLeftPlayer: Player = {
            _id: 'Frodo',
            name: 'Frodo',
            imageURL: '/images/icons/players/frodo-icon.png'
        };
        const expectedRightPlayer: Player = {
            _id: 'Sam',
            name: 'Sam',
            imageURL: '/images/icons/players/samwise-icon.png'
        };

        let welcomePair = wrapper.find('WelcomePair');
        expect(welcomePair.props().leftPlayer).toEqual(expectedLeftPlayer);
        expect(welcomePair.props().rightPlayer).toEqual(expectedRightPlayer);
        expect(wrapper.find(`.${Styles.welcomeProverb}`).text())
            .toEqual('Together, climb mountains.');
    });

    it('will return the dynamic duo when it rolls a one.', function () {
        let randomValue = 1;
        let randomizer = new Randomizer();
        spyOn(randomizer, 'next').and.returnValue(randomValue);
        const wrapper = shallow(<WelcomeSplash randomizer={randomizer} hiddenTag={""}/>);

        const expectedLeftPlayer: Player = {
            _id: 'Batman',
            name: 'Batman',
            imageURL: '/images/icons/players/grayson-icon.png'
        };
        const expectedRightPlayer: Player = {
            _id: 'Robin',
            name: 'Robin',
            imageURL: '/images/icons/players/wayne-icon.png'
        };

        let welcomePair = wrapper.find('WelcomePair');
        expect(welcomePair.props().leftPlayer).toEqual(expectedLeftPlayer);
        expect(welcomePair.props().rightPlayer).toEqual(expectedRightPlayer);
        expect(wrapper.find(`.${Styles.welcomeProverb}`).text())
            .toEqual('Clean up the city, together.');
    });

    it('will return the heroes of WW II when it rolls a two.', function () {
        let randomValue = 2;
        let randomizer = new Randomizer();
        spyOn(randomizer, 'next').and.returnValue(randomValue);
        const wrapper = shallow(<WelcomeSplash randomizer={randomizer} hiddenTag={""}/>);

        const expectedLeftPlayer: Player = {
            _id: 'Rosie',
            name: 'Rosie',
            imageURL: '/images/icons/players/rosie-icon.png'
        };
        const expectedRightPlayer: Player = {
            _id: 'Wendy',
            name: 'Wendy',
            imageURL: '/images/icons/players/wendy-icon.png'
        };

        let welcomePair = wrapper.find('WelcomePair');
        expect(welcomePair.props().leftPlayer).toEqual(expectedLeftPlayer);
        expect(welcomePair.props().rightPlayer).toEqual(expectedRightPlayer);
        expect(wrapper.find(`.${Styles.welcomeProverb}`).text())
            .toEqual('Team up. Get things done.');
    });
});