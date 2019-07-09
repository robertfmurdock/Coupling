import com.zegreatrob.coupling.client.GravatarOptions
import com.zegreatrob.coupling.client.gravatarImage
import com.zegreatrob.coupling.client.playerCard
import com.zegreatrob.coupling.client.playerRoster
import com.zegreatrob.coupling.common.*
import com.zegreatrob.coupling.common.entity.heatmap.CalculateHeatMapCommand
import com.zegreatrob.coupling.common.entity.heatmap.CalculateHeatMapCommandDispatcher
import com.zegreatrob.coupling.common.entity.player.callsign.CallSign
import com.zegreatrob.coupling.common.entity.player.callsign.FindCallSignAction
import com.zegreatrob.coupling.common.entity.player.callsign.FindCallSignActionDispatcher
import org.w3c.dom.events.Event
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
@JsName("PlayerCard")
fun playerCardJs(props: dynamic): dynamic = buildElements {
    playerCard {
        attrs {
            tribeId = props.tribeId.unsafeCast<String>()
            player = props.player.unsafeCast<Json>().toPlayer()
            className = props.className.unsafeCast<String?>()
            size = props.size.unsafeCast<Int>()
            onClick = props.onClick.unsafeCast<Function1<Event, Unit>>()
            pathSetter = props.pathSetter.unsafeCast<Function1<String, Unit>>()
            disabled = props.disabled.unsafeCast<Boolean?>() ?: false
        }
    }
}

@Suppress("unused")
@JsName("PlayerRoster")
fun playerRosterJs(props: dynamic): dynamic = buildElements {
    playerRoster {
        attrs {
            tribeId = props.tribeId.unsafeCast<String>()
            players = props.players.unsafeCast<Array<Json>>().map { it.toPlayer() }
            label = props.label.unsafeCast<String?>()
            className = props.className.unsafeCast<String?>()
            pathSetter = props.pathSetter.unsafeCast<Function1<String, Unit>>()
        }
    }
}