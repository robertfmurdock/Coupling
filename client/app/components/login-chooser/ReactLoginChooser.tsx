import * as React from "react";
import GoogleSignIn from "../../GoogleSignIn";
import * as Styles from "./styles.css";


async function googleSignIn() {
    await GoogleSignIn.signIn();
}

function microsoftSignIn() {
    window.location.pathname = "/microsoft-login"
}

export default function ReactLoginChooser() {
    return <div className={Styles.className}>
        <div>
            <div className="google-login super white button" onClick={googleSignIn}>
                Google
            </div>
        </div>
        <div>
            <div className="ms-login super blue button" onClick={microsoftSignIn}>
                Microsoft
            </div>
        </div>
    </div>
}