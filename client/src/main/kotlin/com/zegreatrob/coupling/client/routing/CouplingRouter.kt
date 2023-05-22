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
import com.zegreatrob.minreact.nfc
import com.zegreatrob.minreact.ntmFC
import js.core.jso
import kotlinx.browser.window
import react.Props
import react.create
import react.dom.html.ReactHTML.div
import react.router.Navigate
import react.router.RouteObject
import react.router.RouterProvider
import react.router.dom.createBrowserRouter
import react.router.useLocation
import react.router.useParams
import react.useMemo

data class CouplingRouter(val animationsDisabled: Boolean, val config: ClientConfig) :
    DataPropsBind<CouplingRouter>(couplingRouter)

val couplingRouter by ntmFC<CouplingRouter> { (animationsDisabled, config) ->
    val (_, isSignedIn, isLoading) = useAuth0Data()
    val browserRouter = useMemo(isSignedIn, config) {
        createBrowserRouter(
            routes = arrayOf(
                couplingRoute("/welcome/", WelcomePage),
                couplingRoute("/about", AboutPage),
                couplingRoute("/demo", DemoPage),
            ).plus(routes(isSignedIn, config)),
            opts = jso { basename = config.basename },
        )
    }
    animationsDisabledContext.Provider(animationsDisabled) {
        if (!isLoading) {
            RouterProvider {
                router = browserRouter
            }
        }
    }
}

private fun routes(isSignedIn: Boolean, config: ClientConfig) = (
    if (isSignedIn) {
        authenticatedRoutes(config)
    } else {
        arrayOf(redirectUnauthenticated())
    }
    )
    .plus(jso<RouteObject> { element = lostRoute.create() })

private fun redirectUnauthenticated(): RouteObject = jso<RouteObject> {
    path = "*"
    element = Navigate.create { to = "/welcome" }
}.also { console.warn("not signed in!!!!", window.location.pathname) }

val lostRoute by nfc<Props> {
    val location = useLocation()
    div { +"Hmm, you seem to be lost. At ${location.pathname}" }
}

private fun authenticatedRoutes(config: ClientConfig): Array<RouteObject> = listOfNotNull(
    jso { path = "/"; element = redirectToParties() },
    if (config.prereleaseMode) couplingRoute("/user", UserPage) else null,
    couplingRoute("/parties/", PartyListPage),
    couplingRoute("/logout/", Logout),
    couplingRoute("/graphiql/", GraphIQLPage),
    couplingRoute("/new-party/", PartyConfigPage),
    jso { path = "/:partyId"; element = redirectToCurrentPairs() },
    couplingRoute("/:partyId/prepare/", PrepareSpinPage),
    couplingRoute("/:partyId/edit/", PartyConfigPage),
    couplingRoute("/:partyId/history", HistoryPage),
    couplingRoute("/:partyId/pins", PinListPage),
    couplingRoute("/:partyId/pin/new", PinPage),
    couplingRoute("/:partyId/pin/:pinId/", PinPage),
    couplingRoute("/:partyId/pairAssignments/current/", CurrentPairsPage),
    couplingRoute("/:partyId/pairAssignments/new", NewPairAssignmentsPage),
    couplingRoute("/:partyId/player/new", PlayerPage),
    couplingRoute("/:partyId/player/:playerId/", PlayerPage),
    couplingRoute("/:partyId/retired-player/:playerId/", RetiredPlayerPage),
    couplingRoute("/:partyId/players/retired", RetiredPlayersPage),
    couplingRoute("/:partyId/statistics", StatisticsPage),
).toTypedArray()

private fun redirectToParties() = Navigate.create { to = "/parties/" }

private fun redirectToCurrentPairs() = Navigate.create {
    val params = useParams()
    to = "/${params["partyId"]}/pairAssignments/current/"
}
