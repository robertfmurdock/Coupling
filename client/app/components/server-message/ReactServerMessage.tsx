import * as Websocket from 'react-websocket';
import * as React from "react";
import {useState} from "react";

const disconnectedMessage = 'Not connected';

function buildSocketUrl(tribeId: any, useSsl: boolean) {
    const protocol = useSsl ? 'wss' : 'ws';
    return `${protocol}://${window.location.host}/api/${tribeId}/pairAssignments/current`;
}

export default function ReactServerMessage(props: { tribeId: string, useSsl: boolean }) {
    const {tribeId, useSsl} = props;
    const [message, setMessage] = useState(disconnectedMessage);

    return <div>
        <Websocket
            url={buildSocketUrl(tribeId, useSsl)}
            onMessage={message => setMessage(message)}
            onClose={() => setMessage(disconnectedMessage)}
        />
        <span>{message}</span>
    </div>
}