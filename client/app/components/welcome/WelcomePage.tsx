import * as React from "react";
import reactDataLoadWrapper from "../ReactDataLoadWrapper/ReactDataLoadWrapper";
import ReactWelcomeView from "./ReactWelcomeView";

const LoadedWelcomePage = reactDataLoadWrapper(ReactWelcomeView);

export default function (props) {
    return <LoadedWelcomePage
        {...props}
    />
}