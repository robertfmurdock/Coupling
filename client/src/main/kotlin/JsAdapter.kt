import com.zegreatrob.coupling.client.*
import com.zegreatrob.coupling.client.pairassignments.*
import com.zegreatrob.coupling.client.pin.pinListPage
import com.zegreatrob.coupling.client.player.playerPage
import com.zegreatrob.coupling.client.player.retiredPlayerPage
import com.zegreatrob.coupling.client.player.retiredPlayersPage
import com.zegreatrob.coupling.client.routing.CouplingRouteProps
import com.zegreatrob.coupling.client.routing.couplingRoute
import com.zegreatrob.coupling.client.stats.statisticsPage
import com.zegreatrob.coupling.client.tribe.tribeConfigPage
import com.zegreatrob.coupling.client.tribe.tribeListPage
import com.zegreatrob.coupling.client.welcome.welcomePage
import com.zegreatrob.coupling.common.*
import com.zegreatrob.coupling.common.entity.heatmap.CalculateHeatMapCommand
import com.zegreatrob.coupling.common.entity.heatmap.CalculateHeatMapCommandDispatcher
import com.zegreatrob.coupling.common.entity.player.callsign.CallSign
import com.zegreatrob.coupling.common.entity.player.callsign.FindCallSignAction
import com.zegreatrob.coupling.common.entity.player.callsign.FindCallSignActionDispatcher
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.promise
import org.w3c.dom.url.URLSearchParams
import react.RBuilder
import react.ReactElement
import react.buildElements
import kotlin.js.Json
import kotlin.js.json

@Suppress("unused")
@JsName("performComposeStatisticsAction")
fun ComposeStatisticsActionDispatcher.performComposeStatisticsAction(tribe: Json, players: Array<Json>, history: Array<Json>) =
        ComposeStatisticsAction(
                tribe.toTribe(),
                players.map { it.toPlayer() },
                history.map { it.toPairAssignmentDocument() }
        )
                .perform()
                .toJson()

@Suppress("unused")
@JsName("commandDispatcher")
fun commandDispatcher(): CommandDispatcher = object : CommandDispatcher {

    @JsName("performFindCallSignAction")
    fun performFindCallSignAction(players: Array<Json>, player: Json) = FindCallSignAction(
            players.map { it.toPlayer() },
            player.toPlayer().run { email ?: id ?: "" }
    ).perform()
            .toJson()

    @JsName("performCalculateHeatMapCommand")
    fun performCalculateHeatMapCommand(
            players: Array<Json>,
            history: Array<Json>,
            rotationPeriod: Int
    ) = CalculateHeatMapCommand(
            players.map { it.toPlayer() },
            historyFromArray(history),
            rotationPeriod
    ).perform()
            .map { it.toTypedArray() }
            .toTypedArray()

}

private fun CallSign.toJson() = json(
        "adjective" to adjective,
        "noun" to noun
)

interface CommandDispatcher : FindCallSignActionDispatcher, CalculateHeatMapCommandDispatcher

@Suppress("unused")
@JsName("components")
object ReactComponents :
        LogoutRenderer,
        PrepareSpinRenderer,
        GoogleSignIn {

    @Suppress("unused")
    @JsName("TribeListPage")
    val tribeListPageJs = jsReactFunction { props ->
        tribeListPage(PageProps(
                props.coupling.unsafeCast<Coupling>(),
                props.pathParams.unsafeCast<Map<String, String>>(),
                pathSetter = props.pathSetter.unsafeCast<Function1<String, Unit>>(),
                search = URLSearchParams(props.search)
        ))
    }

    @Suppress("unused")
    @JsName("WelcomePage")
    val welcomePageJs = jsReactFunction { props ->
        welcomePage(PageProps(
                props.coupling.unsafeCast<Coupling>(),
                props.pathParams.unsafeCast<Map<String, String>>(),
                pathSetter = props.pathSetter.unsafeCast<Function1<String, Unit>>(),
                search = URLSearchParams(props.search)
        ))
    }

    @Suppress("unused")
    @JsName("RetiredPlayersPage")
    val retiredPlayersPageJs = jsReactFunction { props ->
        retiredPlayersPage(PageProps(
                props.coupling.unsafeCast<Coupling>(),
                props.pathParams.unsafeCast<Map<String, String>>(),
                pathSetter = props.pathSetter.unsafeCast<Function1<String, Unit>>(),
                search = URLSearchParams(props.search)
        ))
    }

    @Suppress("unused")
    @JsName("StatisticsPage")
    val statisticsPageJs = jsReactFunction { props ->
        statisticsPage(PageProps(
                props.coupling.unsafeCast<Coupling>(),
                props.pathParams.unsafeCast<Map<String, String>>(),
                pathSetter = props.pathSetter.unsafeCast<Function1<String, Unit>>(),
                search = URLSearchParams(props.search)
        ))
    }

    @Suppress("unused")
    @JsName("TribeConfigPage")
    val tribeConfigPageJs = jsReactFunction { props ->
        tribeConfigPage(PageProps(
                props.coupling.unsafeCast<Coupling>(),
                props.pathParams.unsafeCast<Map<String, String>>(),
                pathSetter = props.pathSetter.unsafeCast<Function1<String, Unit>>(),
                search = URLSearchParams(props.search)
        ))
    }

    @Suppress("unused")
    @JsName("PinListPage")
    val pinListPageJs = jsReactFunction { props ->
        pinListPage(PageProps(
                props.coupling.unsafeCast<Coupling>(),
                props.pathParams.unsafeCast<Map<String, String>>(),
                pathSetter = props.pathSetter.unsafeCast<Function1<String, Unit>>(),
                search = URLSearchParams(props.search)
        ))
    }

    @Suppress("unused")
    @JsName("PlayerPage")
    val playerPageJs = jsReactFunction { props ->
        playerPage(PageProps(
                props.coupling.unsafeCast<Coupling>(),
                props.pathParams.unsafeCast<Map<String, String>>(),
                pathSetter = props.pathSetter.unsafeCast<Function1<String, Unit>>(),
                search = URLSearchParams(props.search)
        ))
    }

    @Suppress("unused")
    @JsName("RetiredPlayerPage")
    val retiredPlayerPageJs = jsReactFunction { props ->
        retiredPlayerPage(PageProps(
                props.coupling.unsafeCast<Coupling>(),
                props.pathParams.unsafeCast<Map<String, String>>(),
                pathSetter = props.pathSetter.unsafeCast<Function1<String, Unit>>(),
                search = URLSearchParams(props.search)
        ))
    }

    @Suppress("unused")
    @JsName("CurrentPairAssignmentsPage")
    val currentPairAssignmentsPageJs = jsReactFunction { props ->
        currentPairAssignmentsPage(PageProps(
                props.coupling.unsafeCast<Coupling>(),
                props.pathParams.unsafeCast<Map<String, String>>(),
                pathSetter = props.pathSetter.unsafeCast<Function1<String, Unit>>(),
                search = URLSearchParams(props.search)
        ))
    }

    @Suppress("unused")
    @JsName("NewPairAssignmentsPage")
    val newPairAssignmentsPageJs = jsReactFunction { props ->
        newPairAssignmentsPage(PageProps(
                props.coupling.unsafeCast<Coupling>(),
                props.pathParams.unsafeCast<Map<String, String>>(),
                pathSetter = props.pathSetter.unsafeCast<Function1<String, Unit>>(),
                search = URLSearchParams(props.search)
        ))
    }

    @Suppress("unused")
    @JsName("HistoryPage")
    val historyPageJs = jsReactFunction { props ->
        historyPage(PageProps(
                props.coupling.unsafeCast<Coupling>(),
                props.pathParams.unsafeCast<Map<String, String>>(),
                pathSetter = props.pathSetter.unsafeCast<Function1<String, Unit>>(),
                search = URLSearchParams(props.search)
        ))
    }

    @Suppress("unused")
    @JsName("PrepareSpinPage")
    val prepareSpinJs = jsReactFunction { props: dynamic ->
        prepareSpinPage(PageProps(
                props.coupling.unsafeCast<Coupling>(),
                props.pathParams.unsafeCast<Map<String, String>>(),
                pathSetter = props.pathSetter.unsafeCast<Function1<String, Unit>>(),
                search = URLSearchParams(props.search)
        ))
    }

    @Suppress("unused")
    @JsName("CouplingRoute")
    val couplingRouteJs = jsReactFunction { props: dynamic ->
        couplingRoute(CouplingRouteProps(
                props.path,
                props.component
        ))
    }

    @Suppress("unused")
    @JsName("Logout")
    val logoutJs = jsReactFunction { props ->
        logout(LogoutProps(props.coupling))
    }

    @Suppress("unused")
    @JsName("googleCheckForSignedIn")
    fun googleCheckForSignedIn(): dynamic = GlobalScope.promise { checkForSignedIn() }

    private fun jsReactFunction(handler: RBuilder.(dynamic) -> ReactElement) = { props: dynamic ->
        buildElements { handler(props) }
    }

}
