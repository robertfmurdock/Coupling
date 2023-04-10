package com.zegreatrob.coupling.client.routing

import com.zegreatrob.coupling.client.AboutPage
import com.zegreatrob.coupling.client.ClientConfig
import com.zegreatrob.coupling.client.components.animationsDisabledContext
import com.zegreatrob.coupling.client.components.external.auth0.react.useAuth0Data
import com.zegreatrob.coupling.client.demo.DemoPage
import com.zegreatrob.coupling.client.graphql.GraphIQLPage
import com.zegreatrob.coupling.client.pairassignments.CurrentPairsPage
import com.zegreatrob.coupling.client.pairassignments.NewPairAssignmentsPage
import com.zegreatrob.coupling.client.pairassignments.list.HistoryPage
import com.zegreatrob.coupling.client.pairassignments.spin.PrepareSpinPage
import com.zegreatrob.coupling.client.party.PartyConfigPage
import com.zegreatrob.coupling.client.party.PartyListPage
import com.zegreatrob.coupling.client.pin.PinListPage
import com.zegreatrob.coupling.client.pin.PinPage
import com.zegreatrob.coupling.client.player.PlayerPage
import com.zegreatrob.coupling.client.player.retired.RetiredPlayerPage
import com.zegreatrob.coupling.client.player.retired.RetiredPlayersPage
import com.zegreatrob.coupling.client.stats.StatisticsPage
import com.zegreatrob.coupling.client.user.Logout
import com.zegreatrob.coupling.client.user.UserPage
import com.zegreatrob.coupling.client.welcome.WelcomePage
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.tmFC
import kotlinx.browser.window
import react.ChildrenBuilder
import react.FC
import react.Props
import react.create
import react.dom.html.ReactHTML.div
import react.router.Navigate
import react.router.PathRoute
import react.router.Routes
import react.router.dom.BrowserRouter
import react.router.useLocation
import react.router.useParams

data class CouplingRouter(val animationsDisabled: Boolean, val config: ClientConfig) :
    DataPropsBind<CouplingRouter>(couplingRouter)

val couplingRouter = tmFC<CouplingRouter> { (animationsDisabled, config) ->
    val (_, isSignedIn, isLoading) = useAuth0Data()
    BrowserRouter {
        basename = config.basename
        com.zegreatrob.coupling.client.components.animationsDisabledContext.Provider(animationsDisabled) {
            if (!isLoading) {
                Routes { routes(isSignedIn, config) }
            }
        }
    }
}

private fun ChildrenBuilder.routes(isSignedIn: Boolean, config: ClientConfig) {
    couplingRoute("/welcome/", WelcomePage)
    couplingRoute("/about", AboutPage)
    couplingRoute("/demo", DemoPage)

    if (isSignedIn) authenticatedRoutes(config) else redirectUnauthenticated()

    PathRoute { element = lostRoute.create() }
}

private fun ChildrenBuilder.redirectUnauthenticated() = PathRoute {
    path = "*"
    element = Navigate.create { to = "/welcome" }
}.also { console.warn("not signed in!!!!", window.location.pathname) }

val lostRoute = FC<Props> {
    val location = useLocation()
    div { +"Hmm, you seem to be lost. At ${location.pathname}" }
}

private fun ChildrenBuilder.authenticatedRoutes(config: ClientConfig) {
    PathRoute { path = "/"; element = redirectToParties() }
    if (config.prereleaseMode) couplingRoute("/user", UserPage)
    couplingRoute("/parties/", PartyListPage)
    couplingRoute("/logout/", Logout)
    couplingRoute("/graphiql/", GraphIQLPage)
    couplingRoute("/new-party/", PartyConfigPage)
    PathRoute { path = "/:partyId"; element = redirectToCurrentPairs() }
    couplingRoute("/:partyId/prepare/", PrepareSpinPage)
    couplingRoute("/:partyId/edit/", PartyConfigPage)
    couplingRoute("/:partyId/history", HistoryPage)
    couplingRoute("/:partyId/pins", PinListPage)
    couplingRoute("/:partyId/pin/new", PinPage)
    couplingRoute("/:partyId/pin/:pinId/", PinPage)
    couplingRoute("/:partyId/pairAssignments/current/", CurrentPairsPage)
    couplingRoute("/:partyId/pairAssignments/new", NewPairAssignmentsPage)
    couplingRoute("/:partyId/player/new", PlayerPage)
    couplingRoute("/:partyId/player/:playerId/", PlayerPage)
    couplingRoute("/:partyId/retired-player/:playerId/", RetiredPlayerPage)
    couplingRoute("/:partyId/players/retired", RetiredPlayersPage)
    couplingRoute("/:partyId/statistics", StatisticsPage)
}

private fun redirectToParties() = Navigate.create { to = "/parties/" }

private fun redirectToCurrentPairs() = Navigate.create {
    val params = useParams()
    to = "/${params["partyId"]}/pairAssignments/current/"
}
