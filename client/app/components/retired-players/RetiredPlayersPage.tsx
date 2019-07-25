// @ts-ignore
import {components} from 'client'
import * as React from "react";
import reactDataLoadWrapper from "../ReactDataLoadWrapper/ReactDataLoadWrapper";

const LoadedRetiredPlayers = reactDataLoadWrapper(components.RetiredPlayers);

export default function (props) {

    const {tribeId, coupling} = props;

    return <LoadedRetiredPlayers
        getDataAsync={async () => {
            const [tribe, retiredPlayers] = await Promise.all([
                coupling.getTribe(tribeId),
                coupling.getRetiredPlayers(tribeId)
            ]);

            return {tribe, retiredPlayers};
        }}
        {...props}
    />
}