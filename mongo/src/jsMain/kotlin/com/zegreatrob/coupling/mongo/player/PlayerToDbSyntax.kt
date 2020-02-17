package com.zegreatrob.coupling.mongo.player

import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.TribeIdPlayer
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.mongo.JsonTimestampSyntax
import com.zegreatrob.coupling.mongo.pin.JsonStringValueSyntax
import com.zegreatrob.coupling.model.Record
import kotlin.js.Json
import kotlin.js.json

interface PlayerToDbSyntax : JsonStringValueSyntax, JsonTimestampSyntax {

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
        Record(
            data = TribeIdPlayer(
                tribeId = TribeId(stringValue("tribe")!!),
                player = fromDbToPlayer()
            ),
            timestamp = timeStamp() ?: DateTime.EPOCH,
            modifyingUserEmail = stringValue("modifiedByUsername") ?: "NOT RECORDED",
            isDeleted = false
        )

    fun Json.fromDbToPlayer() = Player(
        id = stringValue("id") ?: stringValue("_id"),
        badge = this["badge"]?.unsafeCast<Int>(),
        name = stringValue("name"),
        email = stringValue("email"),
        callSignAdjective = stringValue("callSignAdjective"),
        callSignNoun = stringValue("callSignNoun"),
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

