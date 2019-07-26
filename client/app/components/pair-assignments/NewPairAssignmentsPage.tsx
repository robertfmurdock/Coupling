import * as React from "react";
import reactDataLoadWrapper from "../ReactDataLoadWrapper/ReactDataLoadWrapper";
import ReactPairAssignments from "./ReactPairAssignments";
import {Coupling} from "../../services";

const LoadedPairAssignments = reactDataLoadWrapper(ReactPairAssignments);

interface Props {
    tribeId: string
    playerIds: string[]
    coupling: Coupling
    search
    pathSetter: (url: string) => void
}

export default function (props: Props) {
    const {coupling, tribeId, search} = props;
    const playerIds = new URLSearchParams(search).getAll('player');
    return <LoadedPairAssignments
        getDataAsync={async function () {
            const [tribe, players] = await Promise.all([
                coupling.getTribe(tribeId),
                coupling.getPlayers(tribeId)
            ]);

            const selectedPlayers = players.filter(player => playerIds.includes(player._id));

            const pairAssignments = await coupling.spin(selectedPlayers, tribeId);

            return {tribe, players, pairAssignments}
        }
        }
        {...props}
        isNew={true}
    />
}