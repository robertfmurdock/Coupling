// @ts-ignore
import {components} from 'client'

import * as React from "react";
import reactDataLoadWrapper from "../ReactDataLoadWrapper/ReactDataLoadWrapper";
import {Coupling} from "../../services";

const LoadedTribeConfig = reactDataLoadWrapper(components.TribeConfig);

interface Props {
    tribeId: string
    coupling: Coupling
    pathSetter: (string) => void
}

export default function (props: Props) {
    const {coupling, tribeId} = props;
    if (tribeId) {
        return <LoadedTribeConfig
            getDataAsync={async () => ({tribe: await coupling.getTribe(tribeId)})}
            {...props}
        />
    } else {

        const tribe = {
            id: '',
            name: 'New Tribe',
            pairingRule: 1,
            defaultBadgeName: 'Default',
            alternateBadgeName: 'Alternate',
        };

        return <LoadedTribeConfig {...props} tribe={tribe}/>
    }
}