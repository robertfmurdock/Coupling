import * as React from "react";
import reactDataLoadWrapper from "../ReactDataLoadWrapper/ReactDataLoadWrapper";
import ReactTribeConfig from "./ReactTribeConfig";
import {Coupling} from "../../services";

const LoadedTribeList = reactDataLoadWrapper(ReactTribeConfig);

interface Props {
    tribeId: string
    coupling: Coupling
    pathSetter: (string) => void
}

export default function (props: Props) {
    const {coupling, tribeId} = props;
    if (tribeId) {
        return <LoadedTribeList
            getDataAsync={async () => ({tribe: await coupling.getTribe(tribeId)})}
            {...props}
        />
    } else {
        return <ReactTribeConfig {...props} />
    }
}