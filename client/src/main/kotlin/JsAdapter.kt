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
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.promise
import org.w3c.dom.url.URLSearchParams
import react.RBuilder
import react.ReactElement
import react.buildElements
import kotlin.js.Json

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
@JsName("components")
object ReactComponents :
        PrepareSpinRenderer,
        GoogleSignIn {

    @Suppress("unused")
    @JsName("TribeListPage")
    val tribeListPageJs = jsReactFunction { props ->
        tribeListPage(PageProps(
                props.pathParams.unsafeCast<Map<String, String>>(),
                pathSetter = props.pathSetter.unsafeCast<Function1<String, Unit>>(),
                search = URLSearchParams(props.search)
        ))
    }

    @Suppress("unused")
    @JsName("WelcomePage")
    val welcomePageJs = jsReactFunction { props ->
        welcomePage(PageProps(
                props.pathParams.unsafeCast<Map<String, String>>(),
                pathSetter = props.pathSetter.unsafeCast<Function1<String, Unit>>(),
                search = URLSearchParams(props.search)
        ))
    }

    @Suppress("unused")
    @JsName("RetiredPlayersPage")
    val retiredPlayersPageJs = jsReactFunction { props ->
        retiredPlayersPage(PageProps(
                props.pathParams.unsafeCast<Map<String, String>>(),
                pathSetter = props.pathSetter.unsafeCast<Function1<String, Unit>>(),
                search = URLSearchParams(props.search)
        ))
    }

    @Suppress("unused")
    @JsName("StatisticsPage")
    val statisticsPageJs = jsReactFunction { props ->
        statisticsPage(PageProps(
                props.pathParams.unsafeCast<Map<String, String>>(),
                pathSetter = props.pathSetter.unsafeCast<Function1<String, Unit>>(),
                search = URLSearchParams(props.search)
        ))
    }

    @Suppress("unused")
    @JsName("TribeConfigPage")
    val tribeConfigPageJs = jsReactFunction { props ->
        tribeConfigPage(PageProps(
                props.pathParams.unsafeCast<Map<String, String>>(),
                pathSetter = props.pathSetter.unsafeCast<Function1<String, Unit>>(),
                search = URLSearchParams(props.search)
        ))
    }

    @Suppress("unused")
    @JsName("PinListPage")
    val pinListPageJs = jsReactFunction { props ->
        pinListPage(PageProps(
                props.pathParams.unsafeCast<Map<String, String>>(),
                pathSetter = props.pathSetter.unsafeCast<Function1<String, Unit>>(),
                search = URLSearchParams(props.search)
        ))
    }

    @Suppress("unused")
    @JsName("PlayerPage")
    val playerPageJs = jsReactFunction { props ->
        playerPage(PageProps(
                props.pathParams.unsafeCast<Map<String, String>>(),
                pathSetter = props.pathSetter.unsafeCast<Function1<String, Unit>>(),
                search = URLSearchParams(props.search)
        ))
    }

    @Suppress("unused")
    @JsName("RetiredPlayerPage")
    val retiredPlayerPageJs = jsReactFunction { props ->
        retiredPlayerPage(PageProps(
                props.pathParams.unsafeCast<Map<String, String>>(),
                pathSetter = props.pathSetter.unsafeCast<Function1<String, Unit>>(),
                search = URLSearchParams(props.search)
        ))
    }

    @Suppress("unused")
    @JsName("CurrentPairAssignmentsPage")
    val currentPairAssignmentsPageJs = jsReactFunction { props ->
        currentPairAssignmentsPage(PageProps(
                props.pathParams.unsafeCast<Map<String, String>>(),
                pathSetter = props.pathSetter.unsafeCast<Function1<String, Unit>>(),
                search = URLSearchParams(props.search)
        ))
    }

    @Suppress("unused")
    @JsName("NewPairAssignmentsPage")
    val newPairAssignmentsPageJs = jsReactFunction { props ->
        newPairAssignmentsPage(PageProps(
                props.pathParams.unsafeCast<Map<String, String>>(),
                pathSetter = props.pathSetter.unsafeCast<Function1<String, Unit>>(),
                search = URLSearchParams(props.search)
        ))
    }

    @Suppress("unused")
    @JsName("HistoryPage")
    val historyPageJs = jsReactFunction { props ->
        historyPage(PageProps(
                props.pathParams.unsafeCast<Map<String, String>>(),
                pathSetter = props.pathSetter.unsafeCast<Function1<String, Unit>>(),
                search = URLSearchParams(props.search)
        ))
    }

    @Suppress("unused")
    @JsName("PrepareSpinPage")
    val prepareSpinJs = jsReactFunction { props: dynamic ->
        prepareSpinPage(PageProps(
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
    @JsName("CouplingRouter")
    val couplingRouterJs = jsReactFunction { props: dynamic ->
        couplingRouter(CouplingRouterProps(
                props.isSignedIn.unsafeCast<Boolean>(),
                props.animationsDisabled.unsafeCast<Boolean>()
        ))
    }

    @Suppress("unused")
    @JsName("Logout")
    val logoutJs = jsReactFunction { props ->
        logout(PageProps(
                props.pathParams.unsafeCast<Map<String, String>>(),
                pathSetter = props.pathSetter.unsafeCast<Function1<String, Unit>>(),
                search = URLSearchParams(props.search)
        ))
    }

    @Suppress("unused")
    @JsName("googleCheckForSignedIn")
    fun googleCheckForSignedIn(): dynamic = GlobalScope.promise { checkForSignedIn() }

    private fun jsReactFunction(handler: RBuilder.(dynamic) -> ReactElement) = { props: dynamic ->
        buildElements { handler(props) }
    }

}
