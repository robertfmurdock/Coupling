package com.zegreatrob.coupling.common

import com.soywiz.klock.internal.toDate
import com.soywiz.klock.internal.toDateTime
import com.zegreatrob.coupling.common.entity.pairassignmentdocument.*
import com.zegreatrob.coupling.common.entity.pin.Pin
import com.zegreatrob.coupling.common.entity.player.Player
import com.zegreatrob.coupling.common.entity.tribe.KtTribe
import com.zegreatrob.coupling.common.entity.tribe.PairingRule
import com.zegreatrob.coupling.common.entity.tribe.TribeId
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

fun PinnedPlayer.toJson(): Json = player.toJson().apply { this["pins"] = pins.toJson() }

fun Array<Pair<String, Any?>>.pairsToJson() = json(*this)

fun Array<Pair<String, Any?>>.plusIfNotNull(key: String, value: Any?): Array<Pair<String, Any?>> {
    return if (value != null)
        plus(Pair(key, value))
    else
        this
}

private fun List<Pin>.toJson(): Array<Json> = map { it.toJson() }
        .toTypedArray()

private fun Pin.toJson() = json("_id" to _id, "tribe" to tribe, "name" to name)

@Suppress("UNCHECKED_CAST")
fun Json.toPlayer(): Player = Player(
        id = stringValue("_id"),
        badge = this["badge"]?.unsafeCast<Int>(),
        name = stringValue("name"),
        email = stringValue("email"),
        callSignAdjective = stringValue("callSignAdjective"),
        callSignNoun = stringValue("callSignNoun"),
        imageURL = stringValue("imageURL")
)

fun Json.toTribe(): KtTribe = KtTribe(
        id = TribeId(stringValue("id")!!),
        pairingRule = PairingRule.fromValue(this["pairingRule"] as? Int)
)

private fun Json.stringValue(key: String) = this[key]?.toString()

fun Array<Json>.toPins() = map {
    Pin(
            _id = it["_id"]?.toString(),
            name = it["name"]?.toString(),
            tribe = it["tribe"]?.toString()
    )
}

@Suppress("unused")
@JsName("historyFromArray")
fun historyFromArray(history: Array<Json>) =
        history.map {
            it.toPairAssignmentDocument()
        }

fun Json.toPairAssignmentDocument() = PairAssignmentDocument(
        date = this["date"].let { if (it is String) Date(it) else it.unsafeCast<Date>() }.toDateTime(),
        pairs = this["pairs"].unsafeCast<Array<Array<Json>>?>()?.map(::pairFromArray) ?: listOf(),
        id = this["_id"].unsafeCast<String?>()?.let { PairAssignmentDocumentId(it) }
)

@JsName("pairFromArray")
fun pairFromArray(array: Array<Json>) = array.map {
    PinnedPlayer(it.toPlayer(), it["pins"].unsafeCast<Array<Json>?>()?.toPins() ?: emptyList())
}.toPairs()

private fun List<PinnedPlayer>.toPairs() = PinnedCouplingPair(this)

fun StatisticsReport.toJson() = json(
        "spinsUntilFullRotation" to spinsUntilFullRotation,
        "pairReports" to pairReports.map { it.toJson() }.toTypedArray(),
        "medianSpinDuration" to medianSpinDuration?.millisecondsInt
)

fun PairReport.toJson() = json(
        "pair" to pair.asArray().map { it.toJson() }.toTypedArray(),
        "timeSinceLastPaired" to when (timeSinceLastPair) {
            is TimeResultValue -> timeSinceLastPair.time
            NeverPaired -> "NeverPaired"
        }
)

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
