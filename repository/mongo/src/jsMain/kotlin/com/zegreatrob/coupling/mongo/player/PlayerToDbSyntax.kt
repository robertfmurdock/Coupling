package com.zegreatrob.coupling.mongo.player

import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.TribeIdPlayer
import com.zegreatrob.coupling.model.player.defaultPlayer
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.with
import kotlin.js.Json
import kotlin.js.json

interface PlayerToDbSyntax : JsonRecordSyntax {

    fun Player.toDbJson(): Json = emptyArray<Pair<String, Any?>>()
        .plusIfNotNull("id", id)
        .plusIfNotNull("name", name)
        .plusIfNotNull("email", email)
        .plusIfNotNull("badge", badge)
        .plusIfNotNull("callSignAdjective", callSignAdjective)
        .plusIfNotNull("callSignNoun", callSignNoun)
        .plusIfNotNull("imageURL", imageURL)
        .pairsToJson()


    fun Json.toPlayerRecord(): Record<TribeIdPlayer> =
        toDbRecord(
            TribeId(stringValue("tribe")!!).with(
                element = fromDbToPlayer()
            )
        )

    fun Json.fromDbToPlayer() = Player(
        id = stringValue("id") ?: stringValue("_id"),
        badge = this["badge"]?.unsafeCast<Int>() ?: defaultPlayer.badge,
        name = stringValue("name") ?: defaultPlayer.name,
        email = stringValue("email") ?: defaultPlayer.email,
        callSignAdjective = stringValue("callSignAdjective") ?: defaultPlayer.callSignAdjective,
        callSignNoun = stringValue("callSignNoun") ?: defaultPlayer.callSignNoun,
        imageURL = stringValue("imageURL")
    )

    fun Array<Pair<String, Any?>>.plusIfNotNull(key: String, value: Any?): Array<Pair<String, Any?>> {
        return if (value != null)
            plus(Pair(key, value))
        else
            this
    }

    fun Array<Pair<String, Any?>>.pairsToJson() = json(*this)

}

