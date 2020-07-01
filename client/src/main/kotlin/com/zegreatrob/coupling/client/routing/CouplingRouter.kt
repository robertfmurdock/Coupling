package com.zegreatrob.coupling.client.routing

import com.zegreatrob.coupling.client.AboutPage
import com.zegreatrob.coupling.client.animationsDisabledContext
import com.zegreatrob.coupling.client.external.react.reactFunction
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
import react.RBuilder
import react.RProps
import react.buildElement
import react.dom.div
import react.router.dom.*
import kotlin.browser.window

data class CouplingRouterProps(val isSignedIn: Boolean, val animationsDisabled: Boolean) : RProps

val CouplingRouter = reactFunction<CouplingRouterProps> { (isSignedIn, animationsDisabled) ->
    browserRouter {
        animationsDisabledContext.Provider(animationsDisabled) { switch { routes(isSignedIn) } }
    }
}

private fun RBuilder.routes(isSignedIn: Boolean) {
    couplingRoute("/welcome/", WelcomePage)
    couplingRoute("/about", AboutPage)

    if (isSignedIn)
        authenticatedRoutes()
    else
        redirectUnauthenticated()

    lostRoute()
}

private fun RBuilder.redirectUnauthenticated() = redirect(from = "", to = "/welcome")
    .also { console.warn("not signed in!!!!", window.location.pathname) }

private fun RBuilder.lostRoute() = route<RProps>(
    path = "",
    render = { props -> div { +"Hmm, you seem to be lost. At ${props.location.pathname}" } }
)

private fun RBuilder.authenticatedRoutes() = switch {
    route(path = "/", exact = true, render = { redirect(from = "", to = "/tribes/") })
    couplingRoute("/tribes/", TribeListPage)
    couplingRoute("/logout/", Logout)
    couplingRoute("/new-tribe/", TribeConfigPage)
    route("/:tribeId", exact = true, render = ::redirectToCurrentPairs)
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

private fun redirectToCurrentPairs(props: RouteResultProps<TribeRouteProps>) = buildElement {
    redirect(from = "", to = "/${props.match.params.tribeId}/pairAssignments/current/")
}

external interface TribeRouteProps : RProps {
    val tribeId: String
}
