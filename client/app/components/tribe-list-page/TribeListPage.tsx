import * as React from "react";
import ReactTribeList from "../tribe-list/ReactTribeList";
import {Coupling} from "../../services";
import reactDataLoadWrapper from "../ReactDataLoadWrapper/ReactDataLoadWrapper";

const LoadedTribeList = reactDataLoadWrapper(ReactTribeList);

export default function (props: { pathSetter: (string) => void, coupling: Coupling }) {
    const {coupling} = props;
    return <LoadedTribeList
        getDataAsync={async function () {
            const tribes = await coupling.getTribes();
            return {tribes};
        }}
        {...props}
    />
}