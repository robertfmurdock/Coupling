package com.zegreatrob.coupling.dynamo

import com.zegreatrob.coupling.model.TribeRecord
import com.zegreatrob.coupling.model.player.*
import kotlin.js.Json
import kotlin.js.json

interface DynamoPlayerJsonMapping : DynamoDatatypeSyntax, TribeIdDynamoRecordJsonMapping {

    fun TribeRecord<Player>.asDynamoJson() = recordJson().add(data.toDynamoJson())

    fun TribeIdPlayer.toDynamoJson() = json("tribeId" to tribeId.value).add(player.toDynamoJson())

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
        name = getDynamoStringValue("name") ?: defaultPlayer.name,
        email = getDynamoStringValue("email") ?: defaultPlayer.email,
        badge = getDynamoNumberValue("badge")?.toInt() ?: defaultPlayer.badge,
        callSignAdjective = getDynamoStringValue("callSignAdjective") ?: defaultPlayer.callSignAdjective,
        callSignNoun = getDynamoStringValue("callSignNoun") ?: defaultPlayer.callSignNoun,
        imageURL = getDynamoStringValue("imageURL")
    )
}