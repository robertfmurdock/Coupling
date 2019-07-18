
import com.zegreatrob.coupling.client.*
import com.zegreatrob.coupling.client.Components.serverMessage
import com.zegreatrob.coupling.client.pairassignments.PrepareSpinProps
import com.zegreatrob.coupling.client.pairassignments.PrepareSpinRenderer
import com.zegreatrob.coupling.client.pairassignments.PrepareSpinRenderer.Companion.prepareSpin
import com.zegreatrob.coupling.client.player.*
import com.zegreatrob.coupling.client.tribe.TribeBrowserProps
import com.zegreatrob.coupling.client.tribe.TribeCardProps
import com.zegreatrob.coupling.client.tribe.TribeCardRenderer.Companion.tribeCard
import com.zegreatrob.coupling.client.tribe.TribeListProps
import com.zegreatrob.coupling.client.tribe.TribeListRenderer.Companion.tribeList
import com.zegreatrob.coupling.client.tribe.tribeBrowser
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
import react.RProps
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

@JsName("components")
object ReactComponents : RetiredPlayersRenderer,
        PlayerRosterRenderer,
        LoginChooserRenderer,
        LogoutRenderer,
        PrepareSpinRenderer {

    @Suppress("unused")
    @JsName("PrepareSpin")
    val prepareSpinJs = jsReactFunction { props: dynamic ->
        component(prepareSpin, PrepareSpinProps(
                tribe = props.tribe.unsafeCast<Json>().toTribe(),
                players = props.players.unsafeCast<Array<Json>>().map { it.toPlayer() },
                pathSetter = props.pathSetter.unsafeCast<Function1<String, Unit>>(),
                history = props.history.unsafeCast<Array<Json>>().map { it.toPairAssignmentDocument() }
        ))
    }

    @Suppress("unused")
    @JsName("PlayerCard")
    val playerCardJs = jsReactFunction { props: dynamic ->
        component(PlayerCardRenderer.playerCard, PlayerCardProps(
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
        component(tribeCard, TribeCardProps(
                tribe = props.tribe.unsafeCast<Json>().toTribe(),
                pathSetter = props.pathSetter.unsafeCast<Function1<String, Unit>>(),
                size = props.size.unsafeCast<Int?>() ?: 150
        ))
    }

    @Suppress("unused")
    @JsName("TribeList")
    val tribeListJs = jsReactFunction { props ->
        component(tribeList, TribeListProps(
                tribes = props.tribes.unsafeCast<Array<Json>>().map { it.toTribe() },
                pathSetter = props.pathSetter.unsafeCast<Function1<String, Unit>>()
        ))
    }


    private fun jsReactFunction(handler: RBuilder.(dynamic) -> ReactElement) = { props: dynamic ->
        buildElements {
            handler(props)
        }
    }

}

@Suppress("unused")
@JsName("GravatarImage")
fun gravatarImageJs(props: dynamic): dynamic = buildElements {
    gravatarImage(
            props.email as String?,
            props.fallback as String?,
            props.className as String?,
            props.alt as String?,
            props.options.unsafeCast<GravatarOptions>()
    )
}

@Suppress("unused")
@JsName("TribeBrowser")
fun tribeBrowserJs(props: dynamic): dynamic = buildElements {
    element(tribeBrowser, TribeBrowserProps(
            tribe = props.tribe.unsafeCast<Json>().toTribe(),
            pathSetter = props.pathSetter.unsafeCast<Function1<String, Unit>>()
    ))
}

@Suppress("unused")
@JsName("PlayerRoster")
fun playerRosterJs(props: dynamic): dynamic = buildElements {
    with(ReactComponents) {
        element(playerRoster,
                PlayerRosterProps(
                        tribeId = props.tribeId.unsafeCast<String>().let(::TribeId),
                        players = props.players.unsafeCast<Array<Json>>().map { it.toPlayer() },
                        label = props.label.unsafeCast<String?>(),
                        className = props.className.unsafeCast<String?>(),
                        pathSetter = props.pathSetter.unsafeCast<Function1<String, Unit>>()
                )
        )
    }
}

@Suppress("unused")
@JsName("RetiredPlayers")
fun retiredPlayersJs(props: dynamic): dynamic = buildElements {
    with(ReactComponents) {
        element(retiredPlayers, RetiredPlayersProps(
                tribe = props.tribe.unsafeCast<Json>().toTribe(),
                retiredPlayers = props.retiredPlayers.unsafeCast<Array<Json>>().map { it.toPlayer() },
                pathSetter = props.pathSetter.unsafeCast<Function1<String, Unit>>()
        ))
    }
}

@Suppress("unused")
@JsName("ServerMessage")
fun serverMessageJs(props: dynamic): dynamic = buildElements {
    element(serverMessage, ServerMessageProps(
            tribeId = props.tribeId.unsafeCast<String>().let(::TribeId),
            useSsl = props.useSsl.unsafeCast<Boolean>()
    ))
}

@Suppress("unused")
@JsName("LoginChooser")
fun loginChooserJs(): dynamic = with(ReactComponents) {
    buildElements {
        element(loginChooser, object : RProps {})
    }
}

@Suppress("unused")
@JsName("Logout")
fun logoutJs(props: dynamic): dynamic = with(ReactComponents) {
    buildElements {
        element(logout, LogoutRendererProps(props.coupling))
    }
}

@Suppress("unused")
@JsName("googleCheckForSignedIn")
fun googleCheckFoSignedIn(): dynamic = with(ReactComponents) { GlobalScope.promise { checkForSignedIn() } }