import * as React from "react";
import reactDataLoadWrapper from "../ReactDataLoadWrapper/ReactDataLoadWrapper";
import ReactTribeConfig from "./ReactTribeConfig";
import {Coupling} from "../../services";

const LoadedTribeConfig = reactDataLoadWrapper(ReactTribeConfig);

interface Props {
    tribeId: string
    coupling: Coupling
    pathSetter: (string) => void
}

export default function (props: Props) {
    const {coupling, tribeId} = props;
    if (tribeId) {
        return <LoadedTribeConfig
            getDataAsync={async () => ({tribe: await coupling.getTribe(tribeId)})}
            {...props}
        />
    } else {
        return <LoadedTribeConfig {...props} />
    }
}