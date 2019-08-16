package com.zegreatrob.coupling.client.routing

import com.zegreatrob.coupling.client.animationsDisabledContext
import com.zegreatrob.coupling.client.external.react.*
import com.zegreatrob.coupling.client.pairassignments.CurrentPairsPage
import com.zegreatrob.coupling.client.pairassignments.NewPairAssignmentsPage
import com.zegreatrob.coupling.client.pairassignments.list.HistoryPage
import com.zegreatrob.coupling.client.pairassignments.spin.PrepareSpinPage
import com.zegreatrob.coupling.client.pin.PinListPage
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
import react.dom.div
import react.router.dom.browserRouter
import react.router.dom.redirect
import react.router.dom.route
import react.router.dom.switch
import kotlin.browser.window

object CouplingRouter : RComponent<CouplingRouterProps>(provider()), CouplingRouterBuilder

data class CouplingRouterProps(val isSignedIn: Boolean, val animationsDisabled: Boolean) : RProps

interface CouplingRouterBuilder : SimpleComponentRenderer<CouplingRouterProps> {

    override fun RContext<CouplingRouterProps>.render() = reactElement {
        browserRouter {
            animationsDisabledContext.Provider(value = props.animationsDisabled) {
                switch {
                    couplingRoute(CouplingRouteProps(path = "/welcome/", RComponent = WelcomePage))

                    if (props.isSignedIn) {
                        authenticatedRoutes()
                    } else {
                        console.warn("not signed in!!!!", window.location.pathname)
                        redirect(from = "", to = "/welcome")
                    }

                    route<RProps>(path = "", render = { props ->
                        div { +"Hmm, you seem to be lost. At ${props.location.pathname}" }
                    })
                }
            }
        }
    }

    private fun RBuilder.authenticatedRoutes() = switch {
        route(path = "/", exact = true, render = { redirect(from = "", to = "/tribes/") })
        couplingRoute(CouplingRouteProps(path = "/tribes/", RComponent = TribeListPage))
        couplingRoute(CouplingRouteProps(path = "/logout/", RComponent = Logout))
        couplingRoute(CouplingRouteProps(path = "/new-tribe/", RComponent = TribeConfigPage))
        route<RProps>(path = "/:tribeId", exact = true, render = { props ->
            redirect(from = "", to = "/${props.match.params.asDynamic().tribeId}/pairAssignments/current/")
        })
        couplingRoute(CouplingRouteProps(path = "/:tribeId/prepare/", RComponent = PrepareSpinPage))
        couplingRoute(CouplingRouteProps(path = "/:tribeId/edit/", RComponent = TribeConfigPage))
        couplingRoute(CouplingRouteProps(path = "/:tribeId/history", RComponent = HistoryPage))
        couplingRoute(CouplingRouteProps(path = "/:tribeId/pins", RComponent = PinListPage))
        couplingRoute(CouplingRouteProps(path = "/:tribeId/pairAssignments/current/", RComponent = CurrentPairsPage))
        couplingRoute(CouplingRouteProps(path = "/:tribeId/pairAssignments/new", RComponent = NewPairAssignmentsPage))
        couplingRoute(CouplingRouteProps(path = "/:tribeId/player/new", RComponent = PlayerPage))
        couplingRoute(CouplingRouteProps(path = "/:tribeId/player/:playerId/", RComponent = PlayerPage))
        couplingRoute(CouplingRouteProps(path = "/:tribeId/retired-player/:playerId/", RComponent = RetiredPlayerPage))
        couplingRoute(CouplingRouteProps(path = "/:tribeId/statistics", RComponent = StatisticsPage))
        couplingRoute(CouplingRouteProps(path = "/:tribeId/players/retired", RComponent = RetiredPlayersPage))
    }

}
