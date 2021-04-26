package com.zegreatrob.coupling.dynamo

import com.zegreatrob.coupling.model.TribeRecord
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.defaultPlayer
import com.zegreatrob.coupling.model.player.player
import com.zegreatrob.coupling.model.player.tribeId
import kotlin.js.Json
import kotlin.js.json

interface DynamoPlayerJsonMapping : DynamoDatatypeSyntax, TribeIdDynamoRecordJsonMapping {

    fun TribeRecord<Player>.asDynamoJson() = recordJson()
        .add(
            json(
                "tribeId" to data.tribeId.value,
                "timestamp+id" to "${timestamp.isoWithMillis()}+${data.player.id}"
            )
        )
        .add(data.player.toDynamoJson())

    fun Player.toDynamoJson() = nullFreeJson(
        "id" to id,
        "name" to name,
        "email" to email,
        "badge" to badge,
        "callSignAdjective" to callSignAdjective,
        "callSignNoun" to callSignNoun,
        "imageURL" to imageURL
    )

    fun Json.toPlayer() = getDynamoStringValue("id")?.let {
        Player(
            id = it,
            name = getDynamoStringValue("name") ?: "",
            email = getDynamoStringValue("email") ?: "",
            badge = getDynamoNumberValue("badge")?.toInt() ?: defaultPlayer.badge,
            callSignAdjective = getDynamoStringValue("callSignAdjective") ?: "",
            callSignNoun = getDynamoStringValue("callSignNoun") ?: "",
            imageURL = getDynamoStringValue("imageURL")
        )
    }
}