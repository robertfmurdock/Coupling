import * as React from "react";
import reactDataLoadWrapper from "../ReactDataLoadWrapper/ReactDataLoadWrapper";
import ReactRetiredPlayers from "./ReactRetiredPlayers";

const LoadedRetiredPlayers = reactDataLoadWrapper(ReactRetiredPlayers);

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