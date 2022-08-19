package com.zegreatrob.coupling.client.routing

import com.zegreatrob.coupling.client.AboutPage
import com.zegreatrob.coupling.client.ClientConfig
import com.zegreatrob.coupling.client.animationsDisabledContext
import com.zegreatrob.coupling.client.demo.DemoPage
import com.zegreatrob.coupling.client.pairassignments.CurrentPairsPage
import com.zegreatrob.coupling.client.pairassignments.NewPairAssignmentsPage
import com.zegreatrob.coupling.client.pairassignments.list.HistoryPage
import com.zegreatrob.coupling.client.pairassignments.spin.PrepareSpinPage
import com.zegreatrob.coupling.client.party.GraphIQLPage
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
import com.zegreatrob.coupling.components.external.auth0.react.useAuth0Data
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.tmFC
import kotlinx.browser.window
import kotlinx.js.get
import react.ChildrenBuilder
import react.FC
import react.Props
import react.create
import react.dom.html.ReactHTML.div
import react.router.Navigate
import react.router.Route
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
        animationsDisabledContext.Provider(animationsDisabled) {
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

    Route { element = lostRoute.create() }
}

private fun ChildrenBuilder.redirectUnauthenticated() = Route {
    path = "*"
    element = Navigate.create { to = "/welcome" }
}.also { console.warn("not signed in!!!!", window.location.pathname) }

val lostRoute = FC<Props> {
    val location = useLocation()
    div { +"Hmm, you seem to be lost. At ${location.pathname}" }
}

private fun ChildrenBuilder.authenticatedRoutes(config: ClientConfig) {
    Route { path = "/"; element = redirectToParties() }
    if (config.prereleaseMode) couplingRoute("/user", UserPage)
    couplingRoute("/tribes/", PartyListPage)
    couplingRoute("/logout/", Logout)
    couplingRoute("/graphiql/", GraphIQLPage)
    couplingRoute("/new-tribe/", PartyConfigPage)
    Route { path = "/:tribeId"; element = redirectToCurrentPairs() }
    couplingRoute("/:tribeId/prepare/", PrepareSpinPage)
    couplingRoute("/:tribeId/edit/", PartyConfigPage)
    couplingRoute("/:tribeId/history", HistoryPage)
    couplingRoute("/:tribeId/pins", PinListPage)
    couplingRoute("/:tribeId/pin/new", PinPage)
    couplingRoute("/:tribeId/pin/:pinId/", PinPage)
    couplingRoute("/:tribeId/pairAssignments/current/", CurrentPairsPage)
    couplingRoute("/:tribeId/pairAssignments/new", NewPairAssignmentsPage)
    couplingRoute("/:tribeId/player/new", PlayerPage)
    couplingRoute("/:tribeId/player/:playerId/", PlayerPage)
    couplingRoute("/:tribeId/retired-player/:playerId/", RetiredPlayerPage)
    couplingRoute("/:tribeId/players/retired", RetiredPlayersPage)
    couplingRoute("/:tribeId/statistics", StatisticsPage)
}

private fun redirectToParties() = Navigate.create { to = "/tribes/" }

private fun redirectToCurrentPairs() = Navigate.create {
    val params = useParams()
    to = "/${params["tribeId"]}/pairAssignments/current/"
}
