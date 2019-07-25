// @ts-ignore
import {components} from 'client'
// @ts-ignore
import * as client from 'client'

import * as React from "react";
import find from 'ramda/es/find'
import propEq from 'ramda/es/propEq'

import reactDataLoadWrapper from "../ReactDataLoadWrapper/ReactDataLoadWrapper";

import {Coupling} from "../../services";

const commandDispatcher = client.commandDispatcher();

const LoadedPairAssignments = reactDataLoadWrapper(components.PlayerConfig);

interface Props {
    coupling: Coupling
    tribeId: string
    playerId: string
    pathSetter: (url: string) => void
}

export default function (props: Props) {
    const {coupling, tribeId, playerId} = props;
    return <LoadedPairAssignments
        key={playerId}
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