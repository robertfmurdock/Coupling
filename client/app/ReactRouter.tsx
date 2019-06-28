import * as React from "react";
import {BrowserRouter as Router, Route, Link} from "react-router-dom";
import ReactWelcomeView from "./components/welcome/ReactWelcomeView";
import Randomizer from "./Randomizer";

export default function () {
    return <Router>
        <Route path="/welcome" exact component={() => <ReactWelcomeView randomizer={new Randomizer()}/>}/>
    </Router>
}