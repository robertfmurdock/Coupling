package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedPlayer
import kotlin.js.Date
import kotlin.js.Json
import kotlin.js.json

fun PinnedPlayer.toJson(): Json = player.toJson().apply { this["pins"] = pins.toJson() }

fun Array<Pair<String, Any?>>.pairsToJson() = json(*this)

fun toDate(it: Any?) = if (it is String)
    Date(it)
else {
    it.unsafeCast<Date>()
}
