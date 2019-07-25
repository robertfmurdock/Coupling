// @ts-ignore
import {components} from 'client'

import reactDataLoadWrapper from "../ReactDataLoadWrapper/ReactDataLoadWrapper";
import {Coupling} from "../../services";
import * as React from "react";

const LoadedPairAssignments = reactDataLoadWrapper(components.PinList);

export default function (props: { tribeId: string, coupling: Coupling }) {
    const {tribeId, coupling} = props;

    return <LoadedPairAssignments
        getDataAsync={async function () {
            const [tribe, pins] = await Promise.all([
                coupling.getTribe(tribeId),
                coupling.getPins(tribeId)
            ]);

            return {tribe, pins}
        }
        }
    />
}