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
import kotlinx.serialization.ExperimentalSerializationApi
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

@ExperimentalSerializationApi
fun Player.toJson(): Json = format.encodeToDynamic(toSerializable()).unsafeCast<Json>()

@ExperimentalSerializationApi
fun TribeRecord<Player>.toJson(): Json = format.encodeToDynamic(toSerializable()).unsafeCast<Json>()

@ExperimentalSerializationApi
fun Pin.toJson() = format.encodeToDynamic(toSerializable()).unsafeCast<Json>()

@ExperimentalSerializationApi
fun Json.toPlayer(): Player = format.decodeFromDynamic<JsonPlayerData>(asDynamic()).toModel()

@ExperimentalSerializationApi
fun Json.toPin() = format.decodeFromDynamic<JsonPinData>(asDynamic()).toModel()

@ExperimentalSerializationApi
fun Json.toTribe() = format.decodeFromDynamic<JsonTribe>(asDynamic()).toModel()

@ExperimentalSerializationApi
fun Json.toTribeRecord() = format.decodeFromDynamic<JsonTribe>(asDynamic()).toModelRecord()

@ExperimentalSerializationApi
fun Tribe.toJson() = format.encodeToDynamic(toSerializable()).unsafeCast<Json>()

@ExperimentalSerializationApi
fun Record<Tribe>.toJson() = format.encodeToDynamic(toSerializable()).unsafeCast<Json>()

@ExperimentalSerializationApi
fun Record<TribeIdPin>.toJson() = format.encodeToDynamic(toSerializable()).unsafeCast<Json>()

@ExperimentalSerializationApi
fun User.toJson() = format.encodeToDynamic(toSerializable()).unsafeCast<Json>()

@ExperimentalSerializationApi
fun Record<User>.toJson() = format.encodeToDynamic(toSerializable()).unsafeCast<Json>()

@ExperimentalSerializationApi
fun Json.toUserRecord() = format.decodeFromDynamic<JsonUserRecord>(asDynamic()).toModel()

@ExperimentalSerializationApi
fun Json.toPairAssignmentDocument() = format.decodeFromDynamic<JsonPairAssignmentDocument>(asDynamic()).toModel()

@ExperimentalSerializationApi
fun PairAssignmentDocument.toJson() = format.encodeToDynamic(toSerializable()).unsafeCast<Json>()

@ExperimentalSerializationApi
fun TribeRecord<PairAssignmentDocument>.toJson() = format.encodeToDynamic(toSerializable()).unsafeCast<Json>()

fun Message.toJson(): Json = format.encodeToDynamic(toSerializable()).unsafeCast<Json>()

fun Json.toMessage(): Message = format.decodeFromDynamic<JsonMessage>(asDynamic()).toModel()

fun Json.toCouplingServerMessage(): CouplingSocketMessage = format.decodeFromDynamic<JsonCouplingSocketMessage>(
    asDynamic()
).toModel()

@ExperimentalSerializationApi
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

@ExperimentalSerializationApi
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
