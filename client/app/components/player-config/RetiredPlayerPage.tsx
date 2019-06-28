import * as React from "react";
import find from 'ramda/es/find'
import propEq from 'ramda/es/propEq'

import reactDataLoadWrapper from "../ReactDataLoadWrapper/ReactDataLoadWrapper";
import ReactPlayerConfig from "./ReactPlayerConfig";

const LoadedPairAssignments = reactDataLoadWrapper(ReactPlayerConfig);


export default function (props) {
    const {coupling, tribeId, playerId} = props;
    return <LoadedPairAssignments
        getDataAsync={async function () {
            const [tribe, players] = await Promise.all([
                coupling.getTribe(tribeId),
                coupling.getRetiredPlayers(tribeId)
            ]);

            const player = find(propEq('_id', playerId), players);

            return {tribe, players, player}
        }
        }
        {...props}
    />
}