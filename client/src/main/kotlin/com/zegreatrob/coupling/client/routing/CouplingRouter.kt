package com.zegreatrob.coupling.client.routing

import com.zegreatrob.coupling.client.AboutPage
import com.zegreatrob.coupling.client.animationsDisabledContext
import com.zegreatrob.coupling.client.demo.DemoPage
import com.zegreatrob.coupling.client.external.auth0.react.useAuth0Data
import com.zegreatrob.coupling.client.pairassignments.CurrentPairsPage
import com.zegreatrob.coupling.client.pairassignments.NewPairAssignmentsPage
import com.zegreatrob.coupling.client.pairassignments.list.HistoryPage
import com.zegreatrob.coupling.client.pairassignments.spin.PrepareSpinPage
import com.zegreatrob.coupling.client.pin.PinListPage
import com.zegreatrob.coupling.client.pin.PinPage
import com.zegreatrob.coupling.client.player.PlayerPage
import com.zegreatrob.coupling.client.player.retired.RetiredPlayerPage
import com.zegreatrob.coupling.client.player.retired.RetiredPlayersPage
import com.zegreatrob.coupling.client.stats.StatisticsPage
import com.zegreatrob.coupling.client.tribe.GraphIQLPage
import com.zegreatrob.coupling.client.tribe.TribeConfigPage
import com.zegreatrob.coupling.client.tribe.TribeListPage
import com.zegreatrob.coupling.client.user.Logout
import com.zegreatrob.coupling.client.welcome.WelcomePage
import com.zegreatrob.minreact.DataProps
import com.zegreatrob.minreact.tmFC
import kotlinx.browser.window
import org.w3c.dom.get
import react.*
import react.dom.html.ReactHTML.div
import react.router.*
import react.router.dom.BrowserRouter

data class CouplingRouter(val animationsDisabled: Boolean) : DataProps<CouplingRouter> {
    override val component get() = couplingRouter
}

val couplingRouter = tmFC<CouplingRouter> { (animationsDisabled) ->
    val (_, isSignedIn, isLoading) = useAuth0Data()

    BrowserRouter {
        basename = (kotlinx.browser.window["basename"]?.toString() ?: "")
        animationsDisabledContext.Provider(animationsDisabled) {
            if (!isLoading) {
                Routes { routes(isSignedIn) }
            }
        }
    }
}

private fun ChildrenBuilder.routes(isSignedIn: Boolean) {
    couplingRoute("/welcome/", WelcomePage)
    couplingRoute("/about", AboutPage)
    couplingRoute("/demo", DemoPage)

    if (isSignedIn) authenticatedRoutes() else redirectUnauthenticated()

    Route { element = createElement { lostRoute() } }
}

private fun ChildrenBuilder.redirectUnauthenticated() = Route {
    path = "*"
    element = Navigate.create { to = "/welcome" }
}.also { console.warn("not signed in!!!!", window.location.pathname) }

val lostRoute = FC<Props> {
    val location = useLocation()
    div { +"Hmm, you seem to be lost. At ${location.pathname}" }
}

private fun ChildrenBuilder.authenticatedRoutes() {
    Route { path = "/"; element = redirectToTribes() }
    couplingRoute("/tribes/", TribeListPage)
    couplingRoute("/logout/", Logout)
    couplingRoute("/graphiql/", GraphIQLPage)
    couplingRoute("/new-tribe/", TribeConfigPage)
    Route { path = "/:tribeId"; element = redirectToCurrentPairs() }
    couplingRoute("/:tribeId/prepare/", PrepareSpinPage)
    couplingRoute("/:tribeId/edit/", TribeConfigPage)
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

private fun redirectToTribes() = Navigate.create { to = "/tribes/" }

private fun redirectToCurrentPairs() = Navigate.create {
    val params = useParams()
    to = "/${params["tribeId"]}/pairAssignments/current/"
}
