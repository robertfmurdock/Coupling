import * as React from "react";
import find from 'ramda/es/find'
import propEq from 'ramda/es/propEq'

import reactDataLoadWrapper from "../ReactDataLoadWrapper/ReactDataLoadWrapper";
import ReactPlayerConfig from "./ReactPlayerConfig";

// @ts-ignore
import * as client from 'client'

const commandDispatcher = client.commandDispatcher();

const LoadedPairAssignments = reactDataLoadWrapper(ReactPlayerConfig);

export default function (props) {
    const {coupling, tribeId, playerId} = props;
    return <LoadedPairAssignments
        getDataAsync={async function () {
            const [tribe, players] = await Promise.all([
                coupling.getTribe(tribeId),
                coupling.getPlayers(tribeId)
            ]);
            let player = find(propEq('_id', playerId), players);

            if (!player) {
                player = {_id: undefined, tribe: tribe.id, callSignAdjective: null, callSignNoun: null};

                const callSign = commandDispatcher.performFindCallSignAction(players, player);

                player.callSignAdjective = callSign.adjective;
                player.callSignNoun = callSign.noun;
            }

            return {tribe, players, player}
        }
        }
        {...props}
    />
}