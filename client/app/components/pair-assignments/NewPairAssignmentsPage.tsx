import * as React from "react";
import reactDataLoadWrapper from "../ReactDataLoadWrapper/ReactDataLoadWrapper";
import ReactPairAssignments from "./ReactPairAssignments";
import {Coupling} from "../../services";

const LoadedPairAssignments = reactDataLoadWrapper(ReactPairAssignments);

export default function (props: { tribeId: string, playerIds: string[], coupling: Coupling }) {
    const {coupling, tribeId, playerIds} = props;
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