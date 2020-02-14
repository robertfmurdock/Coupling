package com.zegreatrob.coupling.dynamo

import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.TribeIdPlayer
import kotlin.js.Json
import kotlin.js.json

interface DynamoPlayerJsonMapping : DynamoDatatypeSyntax, TribeIdDynamoRecordJsonMapping {
    fun TribeIdPlayer.toDynamoJson() = tribeId.recordJson()
        .add(player.toDynamoJson())

    fun Player.toDynamoJson() = json(
        "id" to id,
        "name" to name,
        "email" to email,
        "badge" to badge,
        "callSignAdjective" to callSignAdjective,
        "callSignNoun" to callSignNoun,
        "imageURL" to imageURL
    )

    fun Json.toPlayer() = Player(
        id = getDynamoStringValue("id")!!,
        name = getDynamoStringValue("name"),
        email = getDynamoStringValue("email"),
        badge = getDynamoNumberValue("badge")?.toInt(),
        callSignAdjective = getDynamoStringValue("callSignAdjective"),
        callSignNoun = getDynamoStringValue("callSignNoun"),
        imageURL = getDynamoStringValue("imageURL")
    )
}