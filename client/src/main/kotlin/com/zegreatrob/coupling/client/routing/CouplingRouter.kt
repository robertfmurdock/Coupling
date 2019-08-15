package com.zegreatrob.coupling.client.routing

import com.zegreatrob.coupling.client.animationsDisabledContext
import com.zegreatrob.coupling.client.external.react.*
import com.zegreatrob.coupling.client.pairassignments.CurrentPairAssignmentsPage
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

object CouplingRouter : ComponentProvider<CouplingRouterProps>(provider()), CouplingRouterBuilder

data class CouplingRouterProps(val isSignedIn: Boolean, val animationsDisabled: Boolean) : RProps

interface CouplingRouterBuilder : SimpleComponentBuilder<CouplingRouterProps> {

    override fun build() = buildBy {
        reactElement {
            browserRouter {
                animationsDisabledContext.Provider(value = props.animationsDisabled) {
                    switch {
                        couplingRoute(CouplingRouteProps(path = "/welcome/", componentProvider = WelcomePage))

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
    }

    private fun RBuilder.authenticatedRoutes() = switch {
        route(path = "/", exact = true, render = { redirect(from = "", to = "/tribes/") })
        couplingRoute(CouplingRouteProps(path = "/tribes/", componentProvider = TribeListPage))
        couplingRoute(CouplingRouteProps(path = "/logout/", componentProvider = Logout))
        couplingRoute(CouplingRouteProps(path = "/new-tribe/", componentProvider = TribeConfigPage))
        route<RProps>(path = "/:tribeId", exact = true, render = { props ->
            redirect(from = "", to = "/${props.match.params.asDynamic().tribeId}/pairAssignments/current/")
        })
        couplingRoute(CouplingRouteProps(path = "/:tribeId/prepare/", componentProvider = PrepareSpinPage))
        couplingRoute(CouplingRouteProps(path = "/:tribeId/edit/", componentProvider = TribeConfigPage))
        couplingRoute(CouplingRouteProps(path = "/:tribeId/history", componentProvider = HistoryPage))
        couplingRoute(CouplingRouteProps(path = "/:tribeId/pins", componentProvider = PinListPage))
        couplingRoute(
            CouplingRouteProps(
                path = "/:tribeId/pairAssignments/current/",
                componentProvider = CurrentPairAssignmentsPage
            )
        )
        couplingRoute(
            CouplingRouteProps(
                path = "/:tribeId/pairAssignments/new",
                componentProvider = NewPairAssignmentsPage
            )
        )
        couplingRoute(CouplingRouteProps(path = "/:tribeId/player/new", componentProvider = PlayerPage))
        couplingRoute(CouplingRouteProps(path = "/:tribeId/player/:playerId/", componentProvider = PlayerPage))
        couplingRoute(
            CouplingRouteProps(
                path = "/:tribeId/retired-player/:playerId/",
                componentProvider = RetiredPlayerPage
            )
        )
        couplingRoute(CouplingRouteProps(path = "/:tribeId/statistics", componentProvider = StatisticsPage))
        couplingRoute(CouplingRouteProps(path = "/:tribeId/players/retired", componentProvider = RetiredPlayersPage))
    }

}
