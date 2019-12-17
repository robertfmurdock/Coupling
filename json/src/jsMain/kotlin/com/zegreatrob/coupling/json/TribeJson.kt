package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.tribe.PairingRule
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import kotlin.js.Json
import kotlin.js.json

fun Json.toTribe(): Tribe =
    Tribe(
        id = TribeId(stringValue("id")!!),
        name = stringValue("name"),
        email = stringValue("email"),
        pairingRule = PairingRule.fromValue(this["pairingRule"]?.toIntFromStringOrInt()),
        defaultBadgeName = stringValue("defaultBadgeName"),
        alternateBadgeName = stringValue("alternateBadgeName"),
        badgesEnabled = this["badgesEnabled"]?.unsafeCast<Boolean>() ?: false,
        callSignsEnabled = this["callSignsEnabled"]?.unsafeCast<Boolean>() ?: false
    )

fun Tribe.toJson() = json(
    "id" to id.value,
    "pairingRule" to PairingRule.toValue(pairingRule),
    "name" to name,
    "email" to email,
    "defaultBadgeName" to defaultBadgeName,
    "alternateBadgeName" to alternateBadgeName,
    "badgesEnabled" to badgesEnabled,
    "callSignsEnabled" to callSignsEnabled
)

val tribeJsonKeys = Tribe(TribeId(""))
    .toJson()
    .getKeys()

private fun Json.getKeys() = js("Object").keys(this).unsafeCast<Array<String>>()

