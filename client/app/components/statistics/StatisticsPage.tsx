// @ts-ignore
import {components} from 'client'
import ReactDataLoadWrapper from "../ReactDataLoadWrapper/ReactDataLoadWrapper";
import * as React from "react";
import {Coupling} from "../../services";

const LoadedTribeStatistics = ReactDataLoadWrapper(components.TribeStatistics);

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