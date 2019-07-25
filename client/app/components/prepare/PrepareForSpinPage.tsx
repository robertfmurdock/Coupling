// @ts-ignore
import {components} from 'client'

import reactDataLoadWrapper from "../ReactDataLoadWrapper/ReactDataLoadWrapper";
import * as React from "react";
import {Coupling} from "../../services";

const LoadedPrepareSpin = reactDataLoadWrapper(components.PrepareSpin);

interface Props {
    coupling: Coupling
    tribeId: string
    pathSetter: (url: String) => void
}

export default function (props: Props) {
    const {tribeId, coupling} = props;

    return <LoadedPrepareSpin
        getDataAsync={async () => {
            const [tribe, players, history] = await Promise.all([
                coupling.getTribe(tribeId),
                coupling.getPlayers(tribeId),
                coupling.getHistory(tribeId)
            ]);
            return {tribe, players, history};
        }}
        {...props}
    />
}