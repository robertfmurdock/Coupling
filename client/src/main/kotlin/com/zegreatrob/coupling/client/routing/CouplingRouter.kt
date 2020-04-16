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
import react.RElementBuilder
import react.RProps
import react.dom.div
import react.router.dom.browserRouter
import react.router.dom.redirect
import react.router.dom.route
import react.router.dom.switch
import kotlin.browser.window

data class CouplingRouterProps(val isSignedIn: Boolean, val animationsDisabled: Boolean) : RProps

val CouplingRouter = reactFunction<CouplingRouterProps> { (isSignedIn, animationsDisabled) ->
    browserRouter {
        animationsDisabledContext.Provider(value = animationsDisabled) {
            switch { routes(isSignedIn) }
        }
    }
}

private fun RElementBuilder<RProps>.routes(isSignedIn: Boolean) {
    couplingRoute(path = "/welcome/", rComponent = WelcomePage)

    if (isSignedIn) {
        authenticatedRoutes()
    } else {
        console.warn("not signed in!!!!", window.location.pathname)
        redirect(from = "", to = "/welcome")
    }

    route<RProps>(path = "", render = { props ->
        div { +"Hmm, you seem to be lost. At ${props.location.pathname}" }
    })
}

private fun RBuilder.authenticatedRoutes() = switch {
    route(path = "/", exact = true, render = { redirect(from = "", to = "/tribes/") })
    couplingRoute("/about", AboutPage)
    couplingRoute("/tribes/", TribeListPage)
    couplingRoute("/logout/", Logout)
    couplingRoute("/new-tribe/", TribeConfigPage)
    route<RProps>("/:tribeId", exact = true, render = { props ->
        redirect(from = "", to = "/${props.match.params.asDynamic().tribeId}/pairAssignments/current/")
    })
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
    couplingRoute("/:tribeId/statistics", StatisticsPage)
    couplingRoute("/:tribeId/players/retired", RetiredPlayersPage)
}
