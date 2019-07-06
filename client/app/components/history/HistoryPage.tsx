import reactDataLoadWrapper from "../ReactDataLoadWrapper/ReactDataLoadWrapper";
import ReactHistory from "./ReactHistory";
import * as React from "react";
import {Coupling} from "../../services";
import PathSetter from "../PathSetter";

const LoadedPairAssignments = reactDataLoadWrapper(ReactHistory);

interface Props {
    tribeId: string
    coupling: Coupling
    pathSetter: PathSetter
}

export default function (props: Props) {
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