import * as React from "react";
import {useState} from "react";
import {Redirect} from "react-router-dom";
import GoogleSignIn from "./GoogleSignIn";
import {Coupling} from "./services";

async function waitForLogout(setLoggedOut, coupling) {
    const data = await Promise.all([
            coupling.logout(),
            GoogleSignIn.signOut()
        ]
    );
    setLoggedOut(data);
}

export default function Logout(props: { coupling: Coupling }) {
    const {coupling} = props;
    const [isLoggedOut, setIsLoggedOut] = useState(false);
    const [logoutPromise, setLogout] = useState(null);

    if (!logoutPromise) {
        setLogout(
            waitForLogout(setIsLoggedOut, coupling)
        );
    }

    return isLoggedOut ? <Redirect to={"/welcome"}/> : <div/>;
}
