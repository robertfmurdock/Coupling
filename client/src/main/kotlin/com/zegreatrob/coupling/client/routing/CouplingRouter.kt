package com.zegreatrob.coupling.client.routing

import com.zegreatrob.coupling.client.AboutPage
import com.zegreatrob.coupling.client.ClientConfig
import com.zegreatrob.coupling.client.components.animationsDisabledContext
import com.zegreatrob.coupling.client.components.external.auth0.react.useAuth0Data
import com.zegreatrob.coupling.client.demo.DemoPage
import com.zegreatrob.coupling.client.demo.LoadingPage
import com.zegreatrob.coupling.client.graphql.GraphIQLPage
import com.zegreatrob.coupling.client.incubating.IncubatingPage
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
import com.zegreatrob.coupling.client.slack.SlackCallbackPage
import com.zegreatrob.coupling.client.slack.SlackConnectPage
import com.zegreatrob.coupling.client.stats.StatisticsPage
import com.zegreatrob.coupling.client.user.Logout
import com.zegreatrob.coupling.client.user.UserPage
import com.zegreatrob.coupling.client.welcome.WelcomePage
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.nfc
import com.zegreatrob.minreact.ntmFC
import js.core.jso
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
                couplingRoute("Welcome", "/welcome/", WelcomePage),
                couplingRoute("About", "/about", AboutPage),
                couplingRoute("Demo", "/demo", DemoPage),
                couplingRoute("Loading Test", "/loading", LoadingPage),
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

private fun redirectUnauthenticated(): RouteObject = jso {
    path = "*"
    element = Navigate.create { to = "/welcome" }
}

val lostRoute by nfc<Props> {
    val location = useLocation()
    div { +"Hmm, you seem to be lost. At ${location.pathname}" }
}

private fun authenticatedRoutes(config: ClientConfig): Array<RouteObject> = listOfNotNull(
    jso { path = "/"; element = redirectToParties() },
    if (config.prereleaseMode) {
        couplingRoute("User", "/user", UserPage)
    } else {
        null
    },
    couplingRoute("Party List", "/parties/", PartyListPage),
    couplingRoute("Logout", "/logout/", Logout),
    couplingRoute("Graph IQL", "/graphiql/", GraphIQLPage),
    couplingRoute("New Party", "/new-party/", PartyConfigPage),
    jso { path = "/:partyId"; element = redirectToCurrentPairs() },
    couplingRoute("Prepare to Spin", "/:partyId/prepare/", PrepareSpinPage),
    couplingRoute("Party Config", "/:partyId/edit/", PartyConfigPage),
    couplingRoute("History", "/:partyId/history", HistoryPage),
    couplingRoute("Pin List", "/:partyId/pins", PinListPage),
    couplingRoute("New Pin", "/:partyId/pin/new", PinPage),
    couplingRoute("Pin Config", "/:partyId/pin/:pinId/", PinPage),
    couplingRoute("Current Pairs", "/:partyId/pairAssignments/current/", CurrentPairsPage),
    couplingRoute("New Pairs", "/:partyId/pairAssignments/new", NewPairAssignmentsPage),
    couplingRoute("New Player", "/:partyId/player/new", PlayerPage),
    couplingRoute("Player Config", "/:partyId/player/:playerId/", PlayerPage),
    couplingRoute("Retired Player Config", "/:partyId/retired-player/:playerId/", RetiredPlayerPage),
    couplingRoute("Retired Player List", "/:partyId/players/retired", RetiredPlayersPage),
    couplingRoute("Statistics", "/:partyId/statistics", StatisticsPage),
    couplingRoute("Incubating", "/incubating", IncubatingPage),
    couplingRoute("SlackCallback", "/integration/slack/connect", SlackConnectPage),
    couplingRoute("SlackCallback", "/integration/slack/callback", SlackCallbackPage),
).toTypedArray()

private fun redirectToParties() = Navigate.create { to = "/parties/" }

private fun redirectToCurrentPairs() = Navigate.create {
    val params = useParams()
    to = "/${params["partyId"]}/pairAssignments/current/"
}
