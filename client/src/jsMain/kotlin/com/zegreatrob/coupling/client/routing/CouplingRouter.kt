package com.zegreatrob.coupling.client.routing

import com.zegreatrob.coupling.client.AboutPage
import com.zegreatrob.coupling.client.ClientConfig
import com.zegreatrob.coupling.client.components.animationsDisabledContext
import com.zegreatrob.coupling.client.components.external.auth0.react.useAuth0Data
import com.zegreatrob.coupling.client.components.thirdPartyAvatarsDisabledContext
import com.zegreatrob.coupling.client.contribution.ContributionListPage
import com.zegreatrob.coupling.client.contribution.ContributionOverviewPage
import com.zegreatrob.coupling.client.contribution.ContributionVisualizationPage
import com.zegreatrob.coupling.client.demo.DemoPage
import com.zegreatrob.coupling.client.demo.LoadingPage
import com.zegreatrob.coupling.client.discord.DiscordCallbackPage
import com.zegreatrob.coupling.client.graphql.GraphIQLPage
import com.zegreatrob.coupling.client.incubating.IncubatingPage
import com.zegreatrob.coupling.client.integration.IntegrationPage
import com.zegreatrob.coupling.client.pairassignments.CurrentPairsPage
import com.zegreatrob.coupling.client.pairassignments.NewPairAssignmentsPage
import com.zegreatrob.coupling.client.pairassignments.list.HistoryPage
import com.zegreatrob.coupling.client.pairassignments.spin.PrepareSpinPage
import com.zegreatrob.coupling.client.party.PartyConfigPage
import com.zegreatrob.coupling.client.party.PartyListPage
import com.zegreatrob.coupling.client.party.PartySecretsPage
import com.zegreatrob.coupling.client.pin.PinListPage
import com.zegreatrob.coupling.client.pin.PinPage
import com.zegreatrob.coupling.client.player.PlayerPage
import com.zegreatrob.coupling.client.player.retired.RetiredPlayersPage
import com.zegreatrob.coupling.client.slack.SlackCallbackPage
import com.zegreatrob.coupling.client.slack.SlackConnectPage
import com.zegreatrob.coupling.client.stats.StatisticsPage
import com.zegreatrob.coupling.client.user.Logout
import com.zegreatrob.coupling.client.user.UserPage
import com.zegreatrob.coupling.client.welcome.WelcomePage
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import js.objects.unsafeJso
import react.FC
import react.Props
import react.create
import react.router.Navigate
import react.router.RouteObject
import react.router.RouterProvider
import react.router.dom.createBrowserRouter
import react.router.useParams
import react.useMemo

external interface CouplingRouterProps : Props {
    var animationsDisabled: Boolean
    var thirdPartyAvatarsDisabled: Boolean
    var config: ClientConfig
}

@ReactFunc
val CouplingRouter by nfc<CouplingRouterProps> { (animationsDisabled, thirdPartyAvatarsDisabled, config) ->
    val (_, isSignedIn, isLoading) = useAuth0Data()
    val browserRouter = useMemo(isSignedIn, config) {
        createBrowserRouter(
            routes = arrayOf(
                config.couplingRoute("/welcome/", "Welcome", WelcomePage),
                config.couplingRoute("/about", "About", AboutPage),
                config.couplingRoute("/demo", "Demo", DemoPage),
                config.couplingRoute("/loading", "Loading Test", LoadingPage),
            ).plus(routes(isSignedIn, config)),
            opts = unsafeJso {
                basename = config.basename
                future = unsafeJso {
                    this.asDynamic()["v7_startTransition"] = true
                    v7_relativeSplatPath = true
                }
            },
        )
    }
    animationsDisabledContext(animationsDisabled) {
        thirdPartyAvatarsDisabledContext(thirdPartyAvatarsDisabled) {
            if (!isLoading) {
                RouterProvider {
                    router = browserRouter
                }
            }
        }
    }
}

private fun routes(isSignedIn: Boolean, config: ClientConfig) = if (isSignedIn) {
    config.authenticatedRoutes()
} else {
    arrayOf(redirectUnauthenticated())
}.plus(unsafeJso<RouteObject> { element = LostRoute.create() })

private fun redirectUnauthenticated(): RouteObject = unsafeJso {
    path = "*"
    element = RedirectUnauthenticated.create()
}

private fun ClientConfig.authenticatedRoutes(): Array<RouteObject> = listOfNotNull(
    unsafeJso {
        path = "/"
        element = navigateToPartyList()
    },
    if (prereleaseMode) {
        couplingRoute("/user", "User", UserPage)
    } else {
        null
    },
    couplingRoute("/parties/", "Party List", PartyListPage),
    couplingRoute("/logout/", "Logout", Logout),
    couplingRoute("/graphiql/", "Graph IQL", GraphIQLPage),
    couplingRoute("/new-party/", "New Party", PartyConfigPage),
    unsafeJso {
        path = "/:partyId"
        element = RedirectToCurrentPairs.create()
    },
    couplingRoute("/:partyId/prepare/", "Prepare to Spin", PrepareSpinPage),
    couplingRoute("/:partyId/edit/", "Party Config", PartyConfigPage),
    couplingRoute("/:partyId/secrets/", "Party Secrets", PartySecretsPage),
    couplingRoute("/:partyId/history", "History", HistoryPage),
    couplingRoute("/:partyId/integrations", "Pin List", IntegrationPage),
    couplingRoute("/:partyId/pins", "Pin List", PinListPage),
    couplingRoute("/:partyId/pin/new", "New Pin", PinPage),
    couplingRoute("/:partyId/pin/:pinId/", "Pin Config", PinPage),
    couplingRoute("/:partyId/pairAssignments/current/", "Current Pairs", CurrentPairsPage),
    couplingRoute("/:partyId/pairAssignments/new", "New Pairs", NewPairAssignmentsPage),
    couplingRoute("/:partyId/player/new", "New Player", PlayerPage),
    couplingRoute("/:partyId/player/:playerId/", "Player Config", PlayerPage),
    couplingRoute("/:partyId/players/retired", "Retired Player List", RetiredPlayersPage),
    couplingRoute("/:partyId/statistics", "Statistics", StatisticsPage),
    couplingRoute("/:partyId/contributions", "Contributions", ContributionOverviewPage),
    couplingRoute("/:partyId/contributions/list", "Contributions", ContributionListPage),
    couplingRoute("/:partyId/contributions/visualization", "Contributions", ContributionVisualizationPage),
    couplingRoute("/incubating", "Incubating", IncubatingPage),
    couplingRoute("/integration/slack/connect", "Slack Connect", SlackConnectPage),
    couplingRoute("/integration/slack/callback", "Slack Callback", SlackCallbackPage),
    couplingRoute("/integration/discord/callback", "Discord Callback", DiscordCallbackPage),
).toTypedArray()

fun navigateToPartyList() = Navigate.create { to = "/parties/" }

val RedirectToCurrentPairs = FC {
    Navigate {
        val params = useParams()
        to = "/${params["partyId"]}/pairAssignments/current/"
    }
}
