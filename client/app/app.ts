// @ts-ignore
import * as logging from 'logging'
// @ts-ignore
import {components} from 'client'

import "prefixfree";
import "./../stylesheets/style.scss";
import "./../stylesheets/animations.scss";
import GoogleSignIn from "./GoogleSignIn";
import * as React from "react";
import * as ReactDOM from "react-dom";

logging.com.zegreatrob.coupling.logging.initializeJasmineLogging(false);

async function bootstrapApp() {
    const isSignedIn = await GoogleSignIn.checkForSignedIn();
    const animationsDisabled = !!window.sessionStorage.getItem('animationDisabled');

    console.log(components.CouplingRouter)

    ReactDOM.render(
        React.createElement(components.CouplingRouter, {isSignedIn, animationsDisabled}),
        document.getElementsByClassName('view-container')[0]
    );
}

bootstrapApp()
    .catch(err => console.log(err));