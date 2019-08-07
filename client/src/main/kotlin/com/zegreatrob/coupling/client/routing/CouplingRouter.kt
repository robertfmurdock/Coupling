package com.zegreatrob.coupling.client.routing

import com.zegreatrob.coupling.client.animationsDisabledContext
import com.zegreatrob.coupling.client.external.react.ComponentBuilder
import com.zegreatrob.coupling.client.external.react.ComponentProvider
import com.zegreatrob.coupling.client.external.react.buildByPls
import com.zegreatrob.coupling.client.pairassignments.CurrentPairAssignmentsPage
import com.zegreatrob.coupling.client.pairassignments.HistoryPage
import com.zegreatrob.coupling.client.pairassignments.NewPairAssignmentsPage
import com.zegreatrob.coupling.client.pairassignments.PrepareSpinPage
import com.zegreatrob.coupling.client.pin.PinListPage
import com.zegreatrob.coupling.client.player.PlayerPage
import com.zegreatrob.coupling.client.player.RetiredPlayerPage
import com.zegreatrob.coupling.client.player.RetiredPlayersPage
import com.zegreatrob.coupling.client.stats.StatisticsPage
import com.zegreatrob.coupling.client.tribe.TribeConfigPage
import com.zegreatrob.coupling.client.tribe.TribeListPage
import com.zegreatrob.coupling.client.user.Logout
import com.zegreatrob.coupling.client.welcome.WelcomePage
import react.RBuilder
import react.RProps
import react.dom.div
import react.router.dom.*
import kotlin.browser.window

object CouplingRouter : ComponentProvider<CouplingRouterProps>(), CouplingRouterBuilder

data class CouplingRouterProps(val isSignedIn: Boolean, val animationsDisabled: Boolean) : RProps

interface CouplingRouterBuilder : ComponentBuilder<CouplingRouterProps> {

    override fun build() = buildByPls {
        {
            browserRouter {
                animationsDisabledContext.Provider(value = props.animationsDisabled) {
                    switch {
                        couplingRoute(CouplingRouteProps(path = "/welcome/", component = WelcomePage.component.rFunction))

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
        couplingRoute(CouplingRouteProps(path = "/tribes/", component = TribeListPage.component.rFunction))
        couplingRoute(CouplingRouteProps(path = "/logout/", component = Logout.component.rFunction))
        couplingRoute(CouplingRouteProps(path = "/new-tribe/", component = TribeConfigPage.component.rFunction))
        route(path = "/:tribeId", exact = true, render = { thing: RouteResultProps<RProps> ->
            redirect(from = "", to = "/${thing.match.params.asDynamic().tribeId}/pairAssignments/current/")
        })
        couplingRoute(CouplingRouteProps(path = "/:tribeId/prepare/", component = PrepareSpinPage.component.rFunction))
        couplingRoute(CouplingRouteProps(path = "/:tribeId/edit/", component = TribeConfigPage.component.rFunction))
        couplingRoute(CouplingRouteProps(path = "/:tribeId/history", component = HistoryPage.component.rFunction))
        couplingRoute(CouplingRouteProps(path = "/:tribeId/pins", component = PinListPage.component.rFunction))
        couplingRoute(CouplingRouteProps(path = "/:tribeId/pairAssignments/current/", component = CurrentPairAssignmentsPage.component.rFunction))
        couplingRoute(CouplingRouteProps(path = "/:tribeId/pairAssignments/new", component = NewPairAssignmentsPage.component.rFunction))
        couplingRoute(CouplingRouteProps(path = "/:tribeId/player/new", component = PlayerPage.component.rFunction))
        couplingRoute(CouplingRouteProps(path = "/:tribeId/player/:playerId/", component = PlayerPage.component.rFunction))
        couplingRoute(CouplingRouteProps(path = "/:tribeId/retired-player/:playerId/", component = RetiredPlayerPage.component.rFunction))
        couplingRoute(CouplingRouteProps(path = "/:tribeId/statistics", component = StatisticsPage.component.rFunction))
        couplingRoute(CouplingRouteProps(path = "/:tribeId/players/retired", component = RetiredPlayersPage.component.rFunction))
    }

}
