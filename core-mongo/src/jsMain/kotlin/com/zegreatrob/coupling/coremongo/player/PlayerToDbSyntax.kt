package com.zegreatrob.coupling.coremongo.player

import com.zegreatrob.coupling.core.entity.player.Player
import com.zegreatrob.coupling.core.json.pairsToJson
import com.zegreatrob.coupling.core.json.plusIfNotNull
import kotlin.js.Json

interface PlayerToDbSyntax {

    fun Player.toDbJson(): Json = emptyArray<Pair<String, Any?>>()
        .plusIfNotNull("id", id)
        .plusIfNotNull("name", name)
        .plusIfNotNull("email", email)
        .plusIfNotNull("badge", badge)
        .plusIfNotNull("callSignAdjective", callSignAdjective)
        .plusIfNotNull("callSignNoun", callSignNoun)
        .plusIfNotNull("imageURL", imageURL)
        .pairsToJson()


    fun Json.fromDbToPlayer(): Player = Player(
        id = stringValue("id") ?: stringValue("_id"),
        badge = this["badge"]?.unsafeCast<Int>(),
        name = stringValue("name"),
        email = stringValue("email"),
        callSignAdjective = stringValue("callSignAdjective"),
        callSignNoun = stringValue("callSignNoun"),
        imageURL = stringValue("imageURL")
    )

    private fun Json.stringValue(key: String) = this[key]?.toString()
}