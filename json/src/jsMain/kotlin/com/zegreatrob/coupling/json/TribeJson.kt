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
        badgesEnabled = this["badgesEnabled"]?.toBoolean() ?: false,
        callSignsEnabled = this["callSignsEnabled"]?.toBoolean() ?: false
    )

private fun Any.toBoolean() = when (this) {
    is String -> isTruthyString()
    is Boolean -> this
    else -> false
}

private fun Any.isTruthyString() = this == "on" || this == "true"

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

