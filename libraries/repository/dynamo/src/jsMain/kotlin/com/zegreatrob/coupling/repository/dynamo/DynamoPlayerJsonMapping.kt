package com.zegreatrob.coupling.repository.dynamo

import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.player.AvatarType
import com.zegreatrob.coupling.model.player.Badge
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.PlayerId
import com.zegreatrob.coupling.model.player.defaultPlayer
import com.zegreatrob.coupling.model.player.player
import kotools.types.text.toNotBlankString
import org.kotools.types.ExperimentalKotoolsTypesApi
import kotlin.js.Json
import kotlin.js.json

interface DynamoPlayerJsonMapping :
    DynamoDatatypeSyntax,
    PartyIdDynamoRecordJsonMapping {

    fun PartyRecord<Player>.asDynamoJson() = recordJson()
        .add(
            json(
                "tribeId" to data.partyId.value.toString(),
                "timestamp+id" to "${timestamp.isoWithMillis()}+${data.player.id}",
            ),
        )
        .add(data.player.toDynamoJson())

    fun Player.toDynamoJson() = nullFreeJson(
        "id" to id.value.toString(),
        "name" to name,
        "email" to email,
        "badge" to badge.value,
        "callSignAdjective" to callSignAdjective,
        "callSignNoun" to callSignNoun,
        "imageURL" to imageURL,
        "avatarType" to avatarType?.toDynamo(),
        "unvalidatedEmails" to additionalEmails.toTypedArray(),
    )

    @OptIn(ExperimentalKotoolsTypesApi::class)
    fun Json.toPlayer() = getDynamoStringValue("id")?.let {
        Player(
            id = PlayerId(it.toNotBlankString().getOrThrow()),
            badge = getDynamoNumberValue("badge")?.toInt()?.let {
                Badge.entries.firstOrNull { badge -> badge.value == it }
            } ?: defaultPlayer.badge,
            name = getDynamoStringValue("name") ?: "",
            email = getDynamoStringValue("email") ?: "",
            callSignAdjective = getDynamoStringValue("callSignAdjective") ?: "",
            callSignNoun = getDynamoStringValue("callSignNoun") ?: "",
            imageURL = getDynamoStringValue("imageURL"),
            avatarType = getDynamoStringValue("avatarType")?.toAvatarType(),
            additionalEmails = getDynamoStringListValue("unvalidatedEmails")?.toSet() ?: emptySet(),
        )
    }
}

private fun AvatarType.toDynamo(): String = when (this) {
    AvatarType.Retro -> "retro"
    AvatarType.RobohashSet1 -> "robohashset1"
    AvatarType.RobohashSet2 -> "robohashset2"
    AvatarType.RobohashSet3 -> "robohashset3"
    AvatarType.RobohashSet4 -> "robohashset4"
    AvatarType.Multiavatar -> "multiavatar"
    AvatarType.DicebearPixelArt -> "dicebearpixelart"
    AvatarType.DicebearAdventurer -> "dicebearadventurer"
    AvatarType.DicebearCroodles -> "dicebearcroodles"
    AvatarType.DicebearThumbs -> "dicebearthumbs"
    AvatarType.DicebearLorelei -> "dicebearlorelei"
    AvatarType.RobohashSet5 -> "robohashset5"
}

private fun String.toAvatarType(): AvatarType? = when (this) {
    "retro" -> AvatarType.Retro
    "robohashset1" -> AvatarType.RobohashSet1
    "robohashset2" -> AvatarType.RobohashSet2
    "robohashset3" -> AvatarType.RobohashSet3
    "robohashset4" -> AvatarType.RobohashSet4
    "multiavatar" -> AvatarType.Multiavatar
    "dicebearpixelart" -> AvatarType.DicebearPixelArt
    "dicebearadventurer" -> AvatarType.DicebearAdventurer
    "dicebearcroodles" -> AvatarType.DicebearCroodles
    "dicebearthumbs" -> AvatarType.DicebearThumbs
    "dicebearlorelei" -> AvatarType.DicebearLorelei
    "robohashset5" -> AvatarType.RobohashSet5
    else -> null
}
