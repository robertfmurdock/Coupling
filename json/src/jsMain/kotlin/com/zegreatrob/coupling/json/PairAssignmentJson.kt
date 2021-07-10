package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedPlayer
import kotlin.js.*

fun PinnedPlayer.toJson(): Json = player.toJson().apply { this["pins"] = pins.toJson() }

fun Array<Pair<String, Any?>>.pairsToJson() = json(*this)

@Suppress("unused")
@JsName("historyFromArray")
fun historyFromArray(history: Array<Json>) =
    history.map {
        it.toPairAssignmentDocument()
    }

fun toDate(it: Any?) = if (it is String)
    Date(it)
else {
    it.unsafeCast<Date>()
}

fun pairFromJson(json: Any) = if (json is Array<*>) {
    PinnedCouplingPair(
        json.unsafeCast<Array<Json>>().map { toPinnedPlayer(it) },
        emptyList()
    )
} else {
    val objectNode = json.unsafeCast<Json>()
    val pins = objectNode["pins"].unsafeCast<Array<Json>?>()?.toPins()
    val pinnedPlayer = objectNode["players"].unsafeCast<Array<Json>>().map { toPinnedPlayer(it) }
    PinnedCouplingPair(pinnedPlayer, pins ?: emptyList())
}

private fun toPinnedPlayer(it: Json) = PinnedPlayer(
    it.toPlayer(),
    it["pins"].unsafeCast<Array<Json>?>()?.toPins() ?: emptyList()
)
