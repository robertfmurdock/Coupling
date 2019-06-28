import * as React from "react";
import {BrowserRouter as Router, Route, Redirect, Link} from "react-router-dom";
import ReactWelcomeView from "./components/welcome/ReactWelcomeView";
import Randomizer from "./Randomizer";
import GoogleSignIn from "./GoogleSignIn";
import TribeListPage from "./components/tribe-list-page/TribeListPage";
import {useRef} from "react";
import {Coupling} from "./services";
import {createBrowserHistory} from "history";
import {useState} from "react";
import TribeConfigPage from "./components/tribe-config/TribeConfigPage";
import PrepareForSpinPage from "./components/prepare/PrepareForSpinPage";
import HistoryPage from "./components/history/HistoryPage";
import PinPage from "./components/pin-list/PinPage";
import CurrentPairAssignmentsPage from "./components/pair-assignments/CurrentPairAssignmentsPage";
import NewPairAssignmentsPage from "./components/pair-assignments/NewPairAssignmentsPage";
import PlayerPage from "./components/player-config/PlayerPage";
import RetiredPlayerPage from "./components/player-config/RetiredPlayerPage";
import StatisticsPage from "./components/statistics/StatisticsPage";
import RetiredPlayersPage from "./components/retired-players/RetiredPlayersPage";

const history = createBrowserHistory();

const pathSetter = path => history.push(path);

async function loadData(setData, coupling) {
    const data = await Promise.all([
            coupling.logout(),
            GoogleSignIn.signOut()
        ]
    );
    setData(data);
}

function Logout(props: { coupling: Coupling }) {
    const {coupling} = props;
    const [isLoggedOut, setIsLoggedOut] = useState(false);
    const [logoutPromise, setLogout] = useState(null);

    if (!logoutPromise) {
        setLogout(
            loadData(setIsLoggedOut, coupling)
        );
    }

    return isLoggedOut ? <Redirect to={"/welcome"}/> : <div/>;

}


export default function (props: { isSignedIn: boolean }) {
    const {isSignedIn} = props;

    let {current: coupling} = useRef(new Coupling());

    return <Router history={history}>
        <Route path="/welcome" exact component={() => <ReactWelcomeView randomizer={new Randomizer()}/>}/>
        {
            isSignedIn
                ? <Redirect to={"/welcome"}/>
                : [
                    <Route path="/" exact component={() => <Redirect to={'/tribes/'}/>}/>,
                    <Route path="/tribes/" exact
                           component={() => <TribeListPage coupling={coupling} pathSetter={pathSetter}/>}/>,
                    <Route path="/logout/" exact component={() => <Logout coupling={coupling}/>}/>,
                    <Route path="/new-tribe/" exact
                           component={() => <TribeConfigPage coupling={coupling} pathSetter={pathSetter}
                                                             tribeId={null}/>}/>,
                    <Route
                        path="/:tribeId/"
                        exact
                        component={props => <Redirect to={`/${props.match.params.tribeId}/pairAssignments/current/`}
                        />}
                    />,
                    <Route
                        path="/:tribeId/prepare/"
                        exact
                        component={props => <PrepareForSpinPage
                            coupling={coupling}
                            tribeId={props.match.params.tribeId}
                        />}
                    />,
                    <Route
                        path="/:tribeId/edit"
                        exact
                        component={props => <TribeConfigPage
                            coupling={coupling}
                            pathSetter={pathSetter}
                            tribeId={props.match.params.tribeId}
                        />}
                    />,
                    <Route
                        path="/:tribeId/history"
                        exact
                        component={props => <HistoryPage
                            coupling={coupling}
                            pathSetter={pathSetter}
                            tribeId={props.match.params.tribeId}
                        />}
                    />,
                    <Route
                        path="/:tribeId/pins"
                        exact
                        component={props => <PinPage
                            coupling={coupling}
                            tribeId={props.match.params.tribeId}
                        />}
                    />,
                    <Route
                        path="/:tribeId/pairAssignments/current/"
                        exact
                        component={props => <CurrentPairAssignmentsPage
                            coupling={coupling}
                            tribeId={props.match.params.tribeId}
                        />}
                    />,
                    <Route
                        path="/:tribeId/pairAssignments/new/"
                        exact
                        component={props => <NewPairAssignmentsPage
                            coupling={coupling}
                            tribeId={props.match.params.tribeId}
                            playerIds={props.location.search.playerId}
                        />}
                    />,
                    <Route
                        path="/:tribeId/player/new/"
                        exact
                        component={props => <PlayerPage
                            coupling={coupling}
                            {...props.match.params}
                        />}
                    />,
                    <Route
                        path="/:tribeId/player/:playerId/"
                        exact
                        component={props => <PlayerPage
                            coupling={coupling}
                            {...props.match.params}
                        />}
                    />,
                    <Route
                        path="/:tribeId/retired-player/:playerId/"
                        exact
                        component={props => <RetiredPlayerPage
                            coupling={coupling}
                            {...props.match.params}
                        />}
                    />,
                    <Route
                        path="/:tribeId/statistics"
                        exact
                        component={props => <StatisticsPage
                            coupling={coupling}
                            {...props.match.params}
                        />}
                    />,
                    <Route
                        path="/:tribeId/players/retired"
                        exact
                        component={props => <RetiredPlayersPage
                            coupling={coupling}
                            {...props.match.params}
                        />}
                    />,
                ]
        }
    </Router>
}