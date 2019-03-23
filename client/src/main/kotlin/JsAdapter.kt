import com.zegreatrob.coupling.common.*
import com.zegreatrob.coupling.common.entity.heatmap.CalculateHeatMapCommand
import com.zegreatrob.coupling.common.entity.heatmap.CalculateHeatMapCommandDispatcher
import com.zegreatrob.coupling.common.entity.player.callsign.CallSign
import com.zegreatrob.coupling.common.entity.player.callsign.FindCallSignAction
import com.zegreatrob.coupling.common.entity.player.callsign.FindCallSignActionDispatcher
import kotlin.js.Json
import kotlin.js.json

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

