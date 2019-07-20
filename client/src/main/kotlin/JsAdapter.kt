
import com.zegreatrob.coupling.client.*
import com.zegreatrob.coupling.client.pairassignments.HistoryProps
import com.zegreatrob.coupling.client.pairassignments.HistorySyntax
import com.zegreatrob.coupling.client.pairassignments.PrepareSpinProps
import com.zegreatrob.coupling.client.pairassignments.PrepareSpinRenderer
import com.zegreatrob.coupling.client.pin.PinListProps
import com.zegreatrob.coupling.client.pin.PinListSyntax
import com.zegreatrob.coupling.client.player.*
import com.zegreatrob.coupling.client.stats.PairReportTableSyntax
import com.zegreatrob.coupling.client.stats.TeamStatisticsProps
import com.zegreatrob.coupling.client.stats.TeamStatisticsSyntax
import com.zegreatrob.coupling.client.tribe.*
import com.zegreatrob.coupling.common.*
import com.zegreatrob.coupling.common.entity.heatmap.CalculateHeatMapCommand
import com.zegreatrob.coupling.common.entity.heatmap.CalculateHeatMapCommandDispatcher
import com.zegreatrob.coupling.common.entity.player.callsign.CallSign
import com.zegreatrob.coupling.common.entity.player.callsign.FindCallSignAction
import com.zegreatrob.coupling.common.entity.player.callsign.FindCallSignActionDispatcher
import com.zegreatrob.coupling.common.entity.tribe.TribeId
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.promise
import org.w3c.dom.events.Event
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
object ReactComponents : RetiredPlayersRenderer,
        PlayerRosterRenderer,
        LoginChooserRenderer,
        LogoutRenderer,
        PlayerCardRenderer,
        TribeCardRenderer,
        TribeBrowserRenderer,
        TribeListRenderer,
        PrepareSpinRenderer,
        ServerMessageRenderer,
        HistorySyntax,
        GoogleSignIn,
        PinListSyntax,
        TeamStatisticsSyntax,
        PairReportTableSyntax {

    @Suppress("unused")
    @JsName("PrepareSpin")
    val prepareSpinJs = jsReactFunction { props: dynamic ->
        prepareSpin(PrepareSpinProps(
                tribe = props.tribe.unsafeCast<Json>().toTribe(),
                players = props.players.unsafeCast<Array<Json>>().map { it.toPlayer() },
                pathSetter = props.pathSetter.unsafeCast<Function1<String, Unit>>(),
                history = props.history.unsafeCast<Array<Json>>().map { it.toPairAssignmentDocument() }
        ))
    }

    @Suppress("unused")
    @JsName("PlayerCard")
    val playerCardJs = jsReactFunction { props: dynamic ->
        playerCard(PlayerCardProps(
                tribeId = TribeId(props.tribeId.unsafeCast<String>()),
                player = props.player.unsafeCast<Json>().toPlayer(),
                className = props.className.unsafeCast<String?>(),
                pathSetter = props.pathSetter.unsafeCast<Function1<String, Unit>>(),
                size = props.size.unsafeCast<Int>(),
                onClick = props.onClick.unsafeCast<Function1<Event, Unit>>(),
                disabled = props.disabled.unsafeCast<Boolean?>() ?: false
        ))
    }

    @Suppress("unused")
    @JsName("TribeCard")
    val tribeCardJs = jsReactFunction { props ->
        tribeCard(TribeCardProps(
                tribe = props.tribe.unsafeCast<Json>().toTribe(),
                pathSetter = props.pathSetter.unsafeCast<Function1<String, Unit>>(),
                size = props.size.unsafeCast<Int?>() ?: 150
        ))
    }

    @Suppress("unused")
    @JsName("TribeList")
    val tribeListJs = jsReactFunction { props ->
        tribeList(TribeListProps(
                tribes = props.tribes.unsafeCast<Array<Json>>().map { it.toTribe() },
                pathSetter = props.pathSetter.unsafeCast<Function1<String, Unit>>()
        ))
    }

    @Suppress("unused")
    @JsName("PlayerRoster")
    val playerRosterJs = jsReactFunction { props ->
        playerRoster(
                PlayerRosterProps(
                        tribeId = props.tribeId.unsafeCast<String>().let(::TribeId),
                        players = props.players.unsafeCast<Array<Json>>().map { it.toPlayer() },
                        label = props.label.unsafeCast<String?>(),
                        className = props.className.unsafeCast<String?>(),
                        pathSetter = props.pathSetter.unsafeCast<Function1<String, Unit>>()
                )
        )
    }

    @Suppress("unused")
    @JsName("TribeBrowser")
    val tribeBrowserJs = jsReactFunction { props ->
        tribeBrowser(TribeBrowserProps(
                tribe = props.tribe.unsafeCast<Json>().toTribe(),
                pathSetter = props.pathSetter.unsafeCast<Function1<String, Unit>>()
        ))
    }

    @Suppress("unused")
    @JsName("RetiredPlayers")
    val retiredPlayersJs = jsReactFunction { props ->
        retiredPlayers(RetiredPlayersProps(
                tribe = props.tribe.unsafeCast<Json>().toTribe(),
                retiredPlayers = props.retiredPlayers.unsafeCast<Array<Json>>().map { it.toPlayer() },
                pathSetter = props.pathSetter.unsafeCast<Function1<String, Unit>>()
        ))
    }

    @Suppress("unused")
    @JsName("ServerMessage")
    val serverMessageJs = jsReactFunction { props ->
        serverMessage(ServerMessageProps(
                tribeId = props.tribeId.unsafeCast<String>().let(::TribeId),
                useSsl = props.useSsl.unsafeCast<Boolean>()
        ))
    }

    @Suppress("unused")
    @JsName("History")
    val historyJs = jsReactFunction { props ->
        history(HistoryProps(
                tribe = props.tribe.unsafeCast<Json>().toTribe(),
                history = props.history.unsafeCast<Array<Json>>().map { it.toPairAssignmentDocument() },
                pathSetter = props.pathSetter.unsafeCast<Function1<String, Unit>>(),
                coupling = props.coupling,
                reload = props.reload.unsafeCast<Function0<Unit>>()
        ))
    }

    @Suppress("unused")
    @JsName("PinList")
    val pinListJs = jsReactFunction { props ->
        pinList(PinListProps(
                tribe = props.tribe.unsafeCast<Json>().toTribe(),
                pins = props.pins.unsafeCast<Array<Json>>().toPins()
        ))
    }

    @Suppress("unused")
    @JsName("TeamStatistics")
    val teamStatisticsJs = jsReactFunction { props ->
        teamStatistics(TeamStatisticsProps(
                spinsUntilFullRotation = props.spinsUntilFullRotation.unsafeCast<Int>(),
                activePlayerCount = props.activePlayerCount.unsafeCast<Int>(),
                medianSpinDuration = props.medianSpinDuration.unsafeCast<String>()
        ))
    }

    @Suppress("unused")
    @JsName("Logout")
    val logoutJs = jsReactFunction { props ->
        logout(LogoutProps(props.coupling))
    }

    @Suppress("unused")
    @JsName("LoginChooser")
    val loginChooserJs = { buildElements { loginChooser() } }

    @Suppress("unused")
    @JsName("googleCheckForSignedIn")
    fun googleCheckForSignedIn(): dynamic = GlobalScope.promise { checkForSignedIn() }

    private fun jsReactFunction(handler: RBuilder.(dynamic) -> ReactElement) = { props: dynamic ->
        buildElements { handler(props) }
    }

}
