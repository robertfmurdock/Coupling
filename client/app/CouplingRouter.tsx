// @ts-ignore
import {components} from 'client'
import * as React from "react";
import {useRef} from "react";
import {BrowserRouter as Router, Route, Redirect, Switch, withRouter} from "react-router-dom";
import {Coupling} from "./services";
import CouplingRoute from "./CouplingRoute";
import AnimationContext from './AnimationContext'
import ServiceContext from './ServiceContext'

export default function CouplingRouter(props: { isSignedIn: boolean, animationsDisabled: boolean }) {
    const {isSignedIn, animationsDisabled} = props;

    return <Router>
        <AnimationContext.Provider value={animationsDisabled}>
            <Switch>
                <Route path="/welcome" exact render={() => <components.WelcomePage/>}/>
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
            <CouplingRoute path={"/tribes/"} component={components.TribeListPage}/>
            <CouplingRoute path={"/logout/"} component={components.Logout}/>
            <CouplingRoute path={"/new-tribe/"} component={components.TribeConfigPage}/>
            <Route path="/:tribeId/" exact render={({match: {params: tribeId}}) =>
                <Redirect to={`/${tribeId}/pairAssignments/current/`}/>}
            />
            <CouplingRoute path={"/:tribeId/prepare/"} component={components.PrepareSpinPage}/>
            <CouplingRoute path={"/:tribeId/edit/"} component={components.TribeConfigPage}/>
            <CouplingRoute path={"/:tribeId/history"} component={components.HistoryPage}/>
            <CouplingRoute path={"/:tribeId/pins"} component={components.PinListPage}/>
            <CouplingRoute path={"/:tribeId/pairAssignments/current/"}
                           component={components.CurrentPairAssignmentsPage}/>
            <CouplingRoute path={"/:tribeId/pairAssignments/new"} component={components.NewPairAssignmentsPage}/>
            <CouplingRoute path={"/:tribeId/player/new/"} component={components.PlayerPage}/>
            <CouplingRoute path={"/:tribeId/player/:playerId/"} component={components.PlayerPage}/>
            <CouplingRoute path={"/:tribeId/retired-player/:playerId/"} component={components.RetiredPlayerPage}/>
            <CouplingRoute path={"/:tribeId/statistics"} component={components.StatisticsPage}/>
            <CouplingRoute path={"/:tribeId/players/retired"} component={components.RetiredPlayersPage}/>
        </Switch>
    </ServiceContext.Provider>;
}
