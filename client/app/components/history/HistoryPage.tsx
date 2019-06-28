import reactDataLoadWrapper from "../ReactDataLoadWrapper/ReactDataLoadWrapper";
import ReactHistory from "./ReactHistory";
import * as React from "react";

const LoadedPairAssignments = reactDataLoadWrapper(ReactHistory);

export default function (props) {
    const {tribeId, coupling} = props;
    return <LoadedPairAssignments
        getDataAsync={async function () {
            const [tribe, history] = await Promise.all([
                coupling.getTribe(tribeId),
                coupling.getHistory(tribeId)
            ]);

            return {tribe, history}
        }
        }
        {...props}
    />
}