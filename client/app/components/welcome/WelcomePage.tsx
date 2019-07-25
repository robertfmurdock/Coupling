// @ts-ignore
import {components} from 'client'
import * as React from "react";
import reactDataLoadWrapper from "../ReactDataLoadWrapper/ReactDataLoadWrapper";

const LoadedWelcomePage = reactDataLoadWrapper(components.Welcome);

export default function (props) {
    return <LoadedWelcomePage
        {...props}
    />
}