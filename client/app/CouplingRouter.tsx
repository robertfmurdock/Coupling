// @ts-ignore
import {components} from 'client'
import * as React from "react";
import {BrowserRouter as Router, Route, Redirect, Switch, withRouter} from "react-router-dom";
import AnimationContext from './AnimationContext'

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
    return <Switch>
        <Route path="/" exact render={() => <Redirect to={'/tribes/'}/>}/>
        <components.CouplingRoute path={"/tribes/"} component={components.TribeListPage}/>
        <components.CouplingRoute path={"/logout/"} component={components.Logout}/>
        <components.CouplingRoute path={"/new-tribe/"} component={components.TribeConfigPage}/>
        <Route path="/:tribeId/" exact render={({match: {params: tribeId}}) =>
            <Redirect to={`/${tribeId}/pairAssignments/current/`}/>}
        />
        <components.CouplingRoute path={"/:tribeId/prepare/"} component={components.PrepareSpinPage}/>
        <components.CouplingRoute path={"/:tribeId/edit/"} component={components.TribeConfigPage}/>
        <components.CouplingRoute path={"/:tribeId/history"} component={components.HistoryPage}/>
        <components.CouplingRoute path={"/:tribeId/pins"} component={components.PinListPage}/>
        <components.CouplingRoute path={"/:tribeId/pairAssignments/current/"}
                                  component={components.CurrentPairAssignmentsPage}/>
        <components.CouplingRoute path={"/:tribeId/pairAssignments/new"} component={components.NewPairAssignmentsPage}/>
        <components.CouplingRoute path={"/:tribeId/player/new/"} component={components.PlayerPage}/>
        <components.CouplingRoute path={"/:tribeId/player/:playerId/"} component={components.PlayerPage}/>
        <components.CouplingRoute path={"/:tribeId/retired-player/:playerId/"}
                                  component={components.RetiredPlayerPage}/>
        <components.CouplingRoute path={"/:tribeId/statistics"} component={components.StatisticsPage}/>
        <components.CouplingRoute path={"/:tribeId/players/retired"} component={components.RetiredPlayersPage}/>
    </Switch>;
}
