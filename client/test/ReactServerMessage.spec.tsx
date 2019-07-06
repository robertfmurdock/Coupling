import {shallow} from 'enzyme';
import * as React from "react";
import * as Websocket from 'react-websocket';
import ReactServerMessage from "../app/components/server-message/ReactServerMessage";

describe('ReactServerMessage', function () {

    function buildComponent(useSsl = false, tribeId = 'LOL') {
        return shallow(<ReactServerMessage tribeId={tribeId} useSsl={useSsl}/>);
    }

    it('connects to current pair assignments websockets', function () {
        const reactServerMessage = buildComponent(false);
        const {url} = reactServerMessage.find(Websocket).props();
        expect(url).toBe(`ws://${window.location.host}/api/LOL/pairAssignments/current`);
    });

    it('connects to current pair assignments websockets security on https', function () {
        const reactServerMessage = buildComponent(true);
        const {url} = reactServerMessage.find(Websocket).props();
        expect(url).toBe(`wss://${window.location.host}/api/LOL/pairAssignments/current`);
    });

    it('will create connection based on tribe id', function () {
        const tribeId = 'bwahahahaha';

        const reactServerMessage = buildComponent(false, tribeId);
        const {url} = reactServerMessage.find(Websocket).props();
        expect(url).toBe(`ws://${window.location.host}/api/${tribeId}/pairAssignments/current`);
    });

    it('displays server message', function () {
        const reactServerMessage = buildComponent();
        const {onMessage} = reactServerMessage.find(Websocket).props();

        const expectedMessage = "Hi it me";
        onMessage(expectedMessage);

        reactServerMessage.update();

        expect(reactServerMessage.find('span').text()).toEqual(expectedMessage);
    });

    it('displays not connected message when socket is closed', function () {
        const reactServerMessage = buildComponent();
        const {onMessage, onClose} = reactServerMessage.find(Websocket).props();

        onMessage('lol');
        reactServerMessage.update();
        onClose();
        reactServerMessage.update();

        expect(reactServerMessage.find('span').text()).toEqual("Not connected");
    });

});