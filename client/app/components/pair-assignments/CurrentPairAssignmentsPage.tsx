import * as React from "react";
import reactDataLoadWrapper from "../ReactDataLoadWrapper/ReactDataLoadWrapper";
import ReactPairAssignments from "./ReactPairAssignments";
import {Coupling} from "../../services";

const LoadedPairAssignments = reactDataLoadWrapper(ReactPairAssignments);

export default function (props: { tribeId: string, coupling: Coupling }) {
    const {coupling, tribeId} = props;

    console.log('current pair assignments whaaat')

    return <LoadedPairAssignments
        getDataAsync={async function () {
            const [tribe, players, [pairAssignments]] = await Promise.all([
                coupling.getTribe(tribeId),
                coupling.getPlayers(tribeId),
                coupling.getHistory(tribeId)
            ]);
            return {tribe, players, pairAssignments}
        }
        }
        {...props}
        isNew={false}
    />
}