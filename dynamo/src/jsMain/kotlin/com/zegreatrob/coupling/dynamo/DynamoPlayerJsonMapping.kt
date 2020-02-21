package com.zegreatrob.coupling.dynamo

import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.TribeIdPlayer
import com.zegreatrob.coupling.model.player.player
import com.zegreatrob.coupling.model.player.tribeId
import kotlin.js.Json

interface DynamoPlayerJsonMapping : DynamoDatatypeSyntax, TribeIdDynamoRecordJsonMapping {
    fun TribeIdPlayer.toDynamoJson() = tribeId.recordJson(player.id)
        .add(player.toDynamoJson())

    fun Player.toDynamoJson() = nullFreeJson(
        "id" to id,
        "name" to name,
        "email" to email,
        "badge" to badge,
        "callSignAdjective" to callSignAdjective,
        "callSignNoun" to callSignNoun,
        "imageURL" to imageURL
    )

    fun Json.toPlayer() = Player(
        id = getDynamoStringValue("id"),
        name = getDynamoStringValue("name"),
        email = getDynamoStringValue("email"),
        badge = getDynamoNumberValue("badge")?.toInt(),
        callSignAdjective = getDynamoStringValue("callSignAdjective"),
        callSignNoun = getDynamoStringValue("callSignNoun"),
        imageURL = getDynamoStringValue("imageURL")
    )
}