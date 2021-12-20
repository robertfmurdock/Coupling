package com.zegreatrob.coupling.client.routing

import com.zegreatrob.coupling.client.AboutPage
import com.zegreatrob.coupling.client.animationsDisabledContext
import com.zegreatrob.coupling.client.demo.DemoPage
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
import com.zegreatrob.coupling.client.tribe.TribeConfigPage
import com.zegreatrob.coupling.client.tribe.TribeListPage
import com.zegreatrob.coupling.client.user.Logout
import com.zegreatrob.coupling.client.welcome.WelcomePage
import com.zegreatrob.coupling.client.reactFunction
import kotlinx.browser.window
import org.w3c.dom.get
import react.*
import react.dom.div
import react.router.*
import react.router.dom.BrowserRouter

data class CouplingRouterProps(val isSignedIn: Boolean, val animationsDisabled: Boolean) : Props

val CouplingRouter = reactFunction<CouplingRouterProps> { (isSignedIn, animationsDisabled) ->
    BrowserRouter {
        attrs.basename = (window["basename"]?.toString() ?: "")
        animationsDisabledContext.Provider(animationsDisabled) {
            Routes { routes(isSignedIn) }
        }
    }
}

private fun RBuilder.routes(isSignedIn: Boolean) {
    couplingRoute("/welcome/", WelcomePage)
    couplingRoute("/about", AboutPage)

    if (isSignedIn)
        authenticatedRoutes()
    else
        redirectUnauthenticated()

    Route { attrs.element = createElement { lostRoute() } }
}

private fun RBuilder.redirectUnauthenticated() = Route {
    attrs.path = "*"
    attrs.element = createElement {
        Navigate { attrs { to = "/welcome" } }
    }
}.also { console.warn("not signed in!!!!", window.location.pathname) }

val lostRoute = fc<Props> {
    val location = useLocation()
    div { +"Hmm, you seem to be lost. At ${location.pathname}" }
}

private fun RBuilder.authenticatedRoutes() {
    Route { attrs { path = "/"; element = redirectToTribes() } }
    couplingRoute("/tribes/", TribeListPage)
    couplingRoute("/logout/", Logout)
    couplingRoute("/new-tribe/", TribeConfigPage)
    Route { attrs { path = "/:tribeId"; element = redirectToCurrentPairs() } }
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
    couplingRoute("/demo", DemoPage)
}

private fun redirectToTribes() = buildElement { Navigate { attrs.to = "/tribes/" } }

private fun redirectToCurrentPairs() = buildElement {
    val params = useParams()
    Navigate { attrs.to = "/${params["tribeId"]}/pairAssignments/current/" }
}
