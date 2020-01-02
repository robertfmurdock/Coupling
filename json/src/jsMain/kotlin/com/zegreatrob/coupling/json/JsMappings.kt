package com.zegreatrob.coupling.json

import com.soywiz.klock.js.toDate
import com.soywiz.klock.js.toDateTime
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedPlayer
import kotlin.js.*

fun PinnedPlayer.toJson(): Json = player.toJson().apply { this["pins"] = pins.toJson() }

fun Array<Pair<String, Any?>>.pairsToJson() = json(*this)

fun Any.toIntFromStringOrInt(): Int? = when (this) {
    is String -> toInt()
    is Int -> this
    else -> null
}

fun Json.stringValue(key: String) = this[key]?.toString()

@Suppress("unused")
@JsName("historyFromArray")
fun historyFromArray(history: Array<Json>) =
    history.map {
        it.toPairAssignmentDocument()
    }

fun Json.toPairAssignmentDocument() = PairAssignmentDocument(
    date = this["date"].let { if (it is String) Date(it) else it.unsafeCast<Date>() }.toDateTime(),
    pairs = this["pairs"].unsafeCast<Array<Array<Json>>?>()?.map(::pairFromArray) ?: listOf(),
    id = this["_id"].unsafeCast<String?>()?.let {
        PairAssignmentDocumentId(
            it
        )
    }
)

@JsName("pairFromArray")
fun pairFromArray(array: Array<Json>) = array.map {
    PinnedPlayer(
        it.toPlayer(),
        it["pins"].unsafeCast<Array<Json>?>()?.toPins() ?: emptyList()
    )
}.toPairs()

private fun List<PinnedPlayer>.toPairs() =
    PinnedCouplingPair(this)

fun PairAssignmentDocument.toJson() = json(
    "_id" to id?.value,
    "date" to date.toDate(),
    "pairs" to toJsPairs()
)

private fun PairAssignmentDocument.toJsPairs() = pairs.map {
    it.players
        .map { player -> player.toJson() }
        .toTypedArray()
}
    .toTypedArray()
