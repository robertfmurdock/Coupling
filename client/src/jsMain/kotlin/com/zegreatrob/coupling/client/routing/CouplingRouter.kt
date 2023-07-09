package com.zegreatrob.coupling.client.routing

import com.zegreatrob.coupling.client.AboutPage
import com.zegreatrob.coupling.client.ClientConfig
import com.zegreatrob.coupling.client.components.animationsDisabledContext
import com.zegreatrob.coupling.client.components.external.auth0.react.useAuth0Data
import com.zegreatrob.coupling.client.demo.DemoPage
import com.zegreatrob.coupling.client.demo.LoadingPage
import com.zegreatrob.coupling.client.graphql.GraphIQLPage
import com.zegreatrob.coupling.client.incubating.IncubatingPage
import com.zegreatrob.coupling.client.integration.IntegrationPage
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
import com.zegreatrob.minreact.ntmFC
import js.core.jso
import react.create
import react.router.Navigate
import react.router.RouteObject
import react.router.RouterProvider
import react.router.dom.createBrowserRouter
import react.router.useParams
import react.useMemo

data class CouplingRouter(val animationsDisabled: Boolean, val config: ClientConfig) :
    DataPropsBind<CouplingRouter>(couplingRouter)

val couplingRouter by ntmFC<CouplingRouter> { (animationsDisabled, config) ->
    val (_, isSignedIn, isLoading) = useAuth0Data()
    val browserRouter = useMemo(isSignedIn, config) {
        createBrowserRouter(
            routes = arrayOf(
                couplingRoute("/welcome/", "Welcome", WelcomePage),
                couplingRoute("/about", "About", AboutPage),
                couplingRoute("/demo", "Demo", DemoPage),
                couplingRoute("/loading", "Loading Test", LoadingPage),
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
    .plus(jso<RouteObject> { element = LostRoute.create() })

private fun redirectUnauthenticated(): RouteObject = jso {
    path = "*"
    element = RedirectUnauthenticated.create()
}

private fun authenticatedRoutes(config: ClientConfig): Array<RouteObject> = listOfNotNull(
    jso { path = "/"; element = redirectToParties() },
    if (config.prereleaseMode) {
        couplingRoute("/user", "User", UserPage)
    } else {
        null
    },
    couplingRoute("/parties/", "Party List", PartyListPage),
    couplingRoute("/logout/", "Logout", Logout),
    couplingRoute("/graphiql/", "Graph IQL", GraphIQLPage),
    couplingRoute("/new-party/", "New Party", PartyConfigPage),
    jso { path = "/:partyId"; element = redirectToCurrentPairs() },
    couplingRoute("/:partyId/prepare/", "Prepare to Spin", PrepareSpinPage),
    couplingRoute("/:partyId/edit/", "Party Config", PartyConfigPage),
    couplingRoute("/:partyId/history", "History", HistoryPage),
    couplingRoute("/:partyId/integrations", "Pin List", IntegrationPage),
    couplingRoute("/:partyId/pins", "Pin List", PinListPage),
    couplingRoute("/:partyId/pin/new", "New Pin", PinPage),
    couplingRoute("/:partyId/pin/:pinId/", "Pin Config", PinPage),
    couplingRoute("/:partyId/pairAssignments/current/", "Current Pairs", CurrentPairsPage),
    couplingRoute("/:partyId/pairAssignments/new", "New Pairs", NewPairAssignmentsPage),
    couplingRoute("/:partyId/player/new", "New Player", PlayerPage),
    couplingRoute("/:partyId/player/:playerId/", "Player Config", PlayerPage),
    couplingRoute("/:partyId/retired-player/:playerId/", "Retired Player Config", RetiredPlayerPage),
    couplingRoute("/:partyId/players/retired", "Retired Player List", RetiredPlayersPage),
    couplingRoute("/:partyId/statistics", "Statistics", StatisticsPage),
    couplingRoute("/incubating", "Incubating", IncubatingPage),
    couplingRoute("/integration/slack/connect", "SlackCallback", SlackConnectPage),
    couplingRoute("/integration/slack/callback", "SlackCallback", SlackCallbackPage),
).toTypedArray()

private fun redirectToParties() = Navigate.create { to = "/parties/" }

private fun redirectToCurrentPairs() = Navigate.create {
    val params = useParams()
    to = "/${params["partyId"]}/pairAssignments/current/"
}