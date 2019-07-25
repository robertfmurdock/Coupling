// @ts-ignore
import {components} from 'client'
import * as React from "react";
import {Coupling} from "../../services";
import reactDataLoadWrapper from "../ReactDataLoadWrapper/ReactDataLoadWrapper";

const LoadedTribeList = reactDataLoadWrapper(components.TribeList);

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