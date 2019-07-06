import * as React from "react";
import {useRef} from "react";
import {BrowserRouter as Router, Route, Redirect, Switch, withRouter} from "react-router-dom";
import Randomizer from "./Randomizer";
import GoogleSignIn from "./GoogleSignIn";
import TribeListPage from "./components/tribe-list-page/TribeListPage";
import {Coupling} from "./services";
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
import WelcomePage from "./components/welcome/WelcomePage";

function pathSetter(history) {
    return path => history.push(path);
}

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

export const AnimationContext: any = React.createContext({
    name: 'animationContext',
});

export default function CouplingRouter(props: { isSignedIn: boolean, animationsDisabled: boolean }) {
    const {isSignedIn, animationsDisabled} = props;

    let {current: coupling} = useRef(new Coupling());

    return <Router>
        <AnimationContext.Provider value={animationsDisabled}>
            <Switch>
                <Route path="/welcome" exact render={() => <WelcomePage randomizer={new Randomizer()}/>}/>
                {
                    isSignedIn
                        ? <Switch>
                            <Route path="/" exact render={() => <Redirect to={'/tribes/'}/>}/>
                            <Route path="/tribes/" exact
                                   render={({history}) => <TribeListPage coupling={coupling}
                                                                         pathSetter={pathSetter(history)}/>}/>
                            <Route path="/logout/" exact render={() => <Logout coupling={coupling}/>}/>
                            <Route path="/new-tribe/" exact
                                   render={({history}) => <TribeConfigPage coupling={coupling}
                                                                           pathSetter={pathSetter(history)}
                                                                           tribeId={null}/>}/>
                            <Route
                                path="/:tribeId/"
                                exact
                                render={props => <Redirect to={`/${props.match.params.tribeId}/pairAssignments/current/`}
                                />}
                            />
                            <Route
                                path="/:tribeId/prepare/"
                                exact
                                render={({match, history}) => <PrepareForSpinPage
                                    coupling={coupling}
                                    pathSetter={pathSetter(history)}
                                    tribeId={match.params.tribeId}
                                />}
                            />
                            <Route
                                path="/:tribeId/edit/"
                                exact
                                render={({match, history}) => <TribeConfigPage
                                    coupling={coupling}
                                    pathSetter={pathSetter(history)}
                                    tribeId={match.params.tribeId}
                                />}
                            />
                            <Route
                                path="/:tribeId/history"
                                exact
                                render={({match, history}) => <HistoryPage
                                    coupling={coupling}
                                    pathSetter={pathSetter(history)}
                                    tribeId={match.params.tribeId}
                                />}
                            />
                            <Route
                                path="/:tribeId/pins"
                                exact
                                render={props => <PinPage
                                    coupling={coupling}
                                    tribeId={props.match.params.tribeId}
                                />}
                            />
                            <Route
                                path="/:tribeId/pairAssignments/current/"
                                exact
                                render={({match, history}) => <CurrentPairAssignmentsPage
                                    coupling={coupling}
                                    pathSetter={pathSetter(history)}
                                    tribeId={match.params.tribeId}
                                />}
                            />
                            <Route
                                path="/:tribeId/pairAssignments/new"
                                exact
                                render={({match, history, location}) => <NewPairAssignmentsPage
                                    coupling={coupling}
                                    tribeId={match.params.tribeId}
                                    pathSetter={pathSetter(history)}
                                    playerIds={new URLSearchParams(location.search).getAll('player')}
                                />}
                            />
                            <Route
                                path="/:tribeId/player/new/"
                                exact
                                render={({match, history}) => <PlayerPage
                                    coupling={coupling}
                                    pathSetter={pathSetter(history)}
                                    locationChanger={() => undefined}
                                    tribeId={match.params.tribeId}
                                    playerId={match.params.playerId}
                                />}
                            />
                            <Route
                                path="/:tribeId/player/:playerId/"
                                exact
                                render={({match, history}) => <PlayerPage
                                    coupling={coupling}
                                    pathSetter={pathSetter(history)}
                                    locationChanger={() => undefined}
                                    tribeId={match.params.tribeId}
                                    playerId={match.params.playerId}
                                />}
                            />
                            <Route
                                path="/:tribeId/retired-player/:playerId/"
                                exact
                                render={({match, history}) => <RetiredPlayerPage
                                    coupling={coupling}
                                    pathSetter={pathSetter(history)}
                                    locationChanger={() => undefined}
                                    tribeId={match.params.tribeId}
                                    playerId={match.params.playerId}
                                />}
                            />
                            <Route
                                path="/:tribeId/statistics"
                                exact
                                render={props => <StatisticsPage
                                    coupling={coupling}
                                    {...props.match.params}
                                />}
                            />
                            <Route
                                path="/:tribeId/players/retired"
                                exact
                                render={props => <RetiredPlayersPage
                                    coupling={coupling}
                                    {...props.match.params}
                                />}
                            />
                        </Switch>
                        : (() => {
                            console.warn('not signed in!!!!', window.location.pathname);
                            return <Redirect to={"/welcome"}/>;
                        })()
                }
                {withRouter(({location}) => <div>Hmm, you seem to be lost. At {location.pathname}</div>)}
            </Switch>
        </AnimationContext.Provider>
    </Router>
}