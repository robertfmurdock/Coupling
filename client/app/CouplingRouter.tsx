// @ts-ignore
import {components} from 'client'
import * as React from "react";
import {useRef} from "react";
import {BrowserRouter as Router, Route, Redirect, Switch, withRouter} from "react-router-dom";
import Randomizer from "./Randomizer";
import TribeListPage from "./components/tribe-list-page/TribeListPage";
import {Coupling} from "./services";
import PrepareForSpinPage from "./components/prepare/PrepareForSpinPage";
import HistoryPage from "./components/history/HistoryPage";
import PinPage from "./components/pin-list/PinPage";
import CurrentPairAssignmentsPage from "./components/pair-assignments/CurrentPairAssignmentsPage";
import NewPairAssignmentsPage from "./components/pair-assignments/NewPairAssignmentsPage";
import PlayerPage from "./components/player-config/PlayerPage";
import RetiredPlayerPage from "./components/player-config/RetiredPlayerPage";
import StatisticsPage from "./components/statistics/StatisticsPage";
import WelcomePage from "./components/welcome/WelcomePage";
import CouplingRoute from "./CouplingRoute";
import Logout from "./Logout";
import AnimationContext from './AnimationContext'
import ServiceContext from './ServiceContext'

export default function CouplingRouter(props: { isSignedIn: boolean, animationsDisabled: boolean }) {
    const {isSignedIn, animationsDisabled} = props;

    return <Router>
        <AnimationContext.Provider value={animationsDisabled}>
            <Switch>
                <Route path="/welcome" exact render={() => <WelcomePage randomizer={new Randomizer()}/>}/>
                {
                    isSignedIn
                        ? <AuthenticatedRoutes/>
                        : (() => {
                            console.warn('not signed in!!!!', window.location.pathname);
                            return <Redirect to={"/welcome"}/>;
                        })()
                }
                <Route
                    render={withRouter(({location}) => <div>Hmm, you seem to be lost. At {location.pathname}</div>)}
                />
            </Switch>
        </AnimationContext.Provider>
    </Router>
}

function AuthenticatedRoutes() {
    let {current: coupling} = useRef(new Coupling());
    return <ServiceContext.Provider value={coupling}>
        <Switch>
            <Route path="/" exact render={() => <Redirect to={'/tribes/'}/>}/>
            <CouplingRoute path={"/tribes/"} component={TribeListPage}/>
            <CouplingRoute path={"/logout/"} component={Logout}/>
            <CouplingRoute path={"/new-tribe/"} component={components.TribeConfigPage}/>
            <Route path="/:tribeId/" exact render={({match: {params: tribeId}}) =>
                <Redirect to={`/${tribeId}/pairAssignments/current/`}/>}
            />
            <CouplingRoute path={"/:tribeId/prepare/"} component={PrepareForSpinPage}/>
            <CouplingRoute path={"/:tribeId/edit/"} component={components.TribeConfigPage}/>
            <CouplingRoute path={"/:tribeId/history"} component={HistoryPage}/>
            <CouplingRoute path={"/:tribeId/pins"} component={PinPage}/>
            <CouplingRoute path={"/:tribeId/pairAssignments/current/"} component={CurrentPairAssignmentsPage}/>
            <CouplingRoute path={"/:tribeId/pairAssignments/new"} component={NewPairAssignmentsPage}/>
            <CouplingRoute path={"/:tribeId/player/new/"} component={PlayerPage}/>
            <CouplingRoute path={"/:tribeId/player/:playerId/"} component={PlayerPage}/>
            <CouplingRoute path={"/:tribeId/retired-player/:playerId/"} component={RetiredPlayerPage}/>
            <CouplingRoute path={"/:tribeId/statistics"} component={StatisticsPage}/>
            <CouplingRoute path={"/:tribeId/players/retired"} component={components.RetiredPlayersPage}/>
        </Switch>
    </ServiceContext.Provider>;
}
