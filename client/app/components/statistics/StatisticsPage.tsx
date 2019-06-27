import ReactDataLoadWrapper from "../ReactDataLoadWrapper/ReactDataLoadWrapper";
import ReactTribeStatistics from "./ReactTribeStatistics";
import * as React from "react";
import {Coupling} from "../../services";


const LoadedTribeStatistics = ReactDataLoadWrapper(ReactTribeStatistics);

export default function (props: { tribeId: string, coupling: Coupling }) {
    const {tribeId, coupling} = props;

    return <LoadedTribeStatistics
        getDataAsync={async () => {
            const [tribe, players, history] = await Promise.all([
                coupling.getTribe(tribeId),
                coupling.getPlayers(tribeId),
                coupling.getHistory(tribeId)
            ]);
            return {tribe, players, history}
        }
        }
        {...props}
    />
}