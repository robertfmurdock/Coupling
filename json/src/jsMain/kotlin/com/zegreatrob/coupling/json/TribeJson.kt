package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.tribe.PairingRule
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.defaultTribe
import kotlin.js.Json
import kotlin.js.json

fun Json.toTribe(): Tribe =
    Tribe(
        id = TribeId(stringValue("id") ?: ""),
        name = stringValue("name"),
        email = stringValue("email"),
        pairingRule = PairingRule.fromValue(this["pairingRule"]?.toIntFromStringOrInt()),
        defaultBadgeName = stringValue("defaultBadgeName") ?: defaultTribe.defaultBadgeName,
        alternateBadgeName = stringValue("alternateBadgeName") ?: defaultTribe.alternateBadgeName,
        badgesEnabled = this["badgesEnabled"]?.toBoolean() ?: defaultTribe.badgesEnabled,
        callSignsEnabled = this["callSignsEnabled"]?.toBoolean() ?: defaultTribe.callSignsEnabled,
        animationEnabled = this["animationsEnabled"]?.toBoolean() ?: defaultTribe.animationEnabled,
        animationSpeed = this["animationSpeed"]?.toDouble() ?: defaultTribe.animationSpeed
    )

private fun Any.toBoolean() = when (this) {
    is String -> isTruthyString()
    is Boolean -> this
    else -> false
}

private fun Any.toDouble() = when (this) {
    is String -> toDoubleOrNull()
    is Double -> this
    else -> null
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
    "callSignsEnabled" to callSignsEnabled,
    "animationsEnabled" to animationEnabled,
    "animationSpeed" to animationSpeed
)

val tribeJsonKeys
    get() = Tribe(TribeId(""))
        .toJson()
        .getKeys()

val tribeRecordJsonKeys get() = tribeJsonKeys + recordJsonKeys