package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.CouplingSocketMessage
import com.zegreatrob.coupling.model.Message
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.TribeRecord
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.pin.TribeIdPin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.model.user.User
import kotlinx.serialization.json.decodeFromDynamic
import kotlinx.serialization.json.encodeToDynamic
import kotlin.js.Json

private val format = kotlinx.serialization.json.Json {
    isLenient = true
    ignoreUnknownKeys = true
    encodeDefaults = true
    coerceInputValues = true
}

val couplingJsonFormat = format

fun Player.toJson(): Json = format.encodeToDynamic(toSerializable()).unsafeCast<Json>()

fun TribeRecord<Player>.toJson(): Json = format.encodeToDynamic(toSerializable()).unsafeCast<Json>()

fun Pin.toJson() = format.encodeToDynamic(toSerializable()).unsafeCast<Json>()

fun Json.toPlayer(): Player = format.decodeFromDynamic<JsonPlayerData>(asDynamic()).toModel()

fun Json.toPin() = format.decodeFromDynamic<JsonPinData>(asDynamic()).toModel()

fun Json.toTribe() = format.decodeFromDynamic<JsonTribe>(asDynamic()).toModel()

fun Tribe.toJson() = format.encodeToDynamic(toSerializable()).unsafeCast<Json>()

fun Record<Tribe>.toJson() = format.encodeToDynamic(toSerializable()).unsafeCast<Json>()

fun Record<TribeIdPin>.toJson() = format.encodeToDynamic(toSerializable()).unsafeCast<Json>()

fun User.toJson() = format.encodeToDynamic(toSerializable()).unsafeCast<Json>()

fun Record<User>.toJson() = format.encodeToDynamic(toSerializable()).unsafeCast<Json>()

fun Json.toPairAssignmentDocument() = format.decodeFromDynamic<JsonPairAssignmentDocument>(asDynamic()).toModel()

fun PairAssignmentDocument.toJson() = format.encodeToDynamic(toSerializable()).unsafeCast<Json>()

fun TribeRecord<PairAssignmentDocument>.toJson() = format.encodeToDynamic(toSerializable()).unsafeCast<Json>()

fun Message.toJson(): Json = format.encodeToDynamic(toSerializable()).unsafeCast<Json>()

fun Json.toMessage(): Message = format.decodeFromDynamic<JsonMessage>(asDynamic()).toModel()

fun Json.toCouplingServerMessage(): CouplingSocketMessage = format.decodeFromDynamic<JsonCouplingSocketMessage>(
    asDynamic()
).toModel()

val playerJsonKeys = Player(
    id = "1",
    badge = 1,
    name = "stub",
    email = "stub",
    callSignAdjective = "stub",
    callSignNoun = "stub",
    imageURL = "stub",
)
    .toJson()
    .getKeys()

val playerRecordJsonKeys = TribeRecord(
    TribeId("").with(
        Player(
            id = "1",
            badge = 1,
            name = "stub",
            email = "stub",
            callSignAdjective = "stub",
            callSignNoun = "stub",
            imageURL = "stub",
        )
    ),
    ""
)
    .toJson()
    .getKeys()
