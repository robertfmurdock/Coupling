package com.zegreatrob.coupling.json

import com.soywiz.klock.js.toDate
import com.soywiz.klock.js.toDateTime
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedPlayer
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import kotlin.js.*

fun Player.toJson(): Json = emptyArray<Pair<String, Any?>>()
    .plusIfNotNull("_id", id)
    .plusIfNotNull("name", name)
    .plusIfNotNull("email", email)
    .plusIfNotNull("badge", badge)
    .plusIfNotNull("callSignAdjective", callSignAdjective)
    .plusIfNotNull("callSignNoun", callSignNoun)
    .plusIfNotNull("imageURL", imageURL)
    .pairsToJson()

fun List<Player>.toJsonArray() = map { it.toJson() }
    .toTypedArray()

fun PinnedPlayer.toJson(): Json = player.toJson().apply { this["pins"] = pins.toJson() }

fun Array<Pair<String, Any?>>.pairsToJson() = json(*this)

fun Array<Pair<String, Any?>>.plusIfNotNull(key: String, value: Any?) = if (value != null)
    plus(Pair(key, value))
else
    this

private fun List<Pin>.toJson(): Array<Json> = map { it.toJson() }
    .toTypedArray()

fun Pin.toJson() = json("_id" to _id, "tribe" to tribe, "name" to name)

fun List<Pin>.toJsonArray() = map { it.toJson() }
    .toTypedArray()

@Suppress("UNCHECKED_CAST")
fun Json.toPlayer(): Player = Player(
    id = stringValue("_id"),
    badge = this["badge"]?.toIntFromStringOrInt(),
    name = stringValue("name"),
    email = stringValue("email"),
    callSignAdjective = stringValue("callSignAdjective"),
    callSignNoun = stringValue("callSignNoun"),
    imageURL = stringValue("imageURL")
)

fun Any.toIntFromStringOrInt(): Int? = when (this) {
    is String -> toInt()
    is Int -> this
    else -> null
}

fun Json.stringValue(key: String) = this[key]?.toString()

fun Array<Json>.toPins() = map { it.toPin() }

fun Json.toPin() = Pin(
    _id = this["_id"]?.toString(),
    name = this["name"]?.toString(),
    tribe = this["tribe"]?.toString()
)

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
