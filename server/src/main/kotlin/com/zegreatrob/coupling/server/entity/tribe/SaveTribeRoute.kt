package com.zegreatrob.coupling.server.entity.tribe

import com.zegreatrob.coupling.json.stringValue
import com.zegreatrob.coupling.json.toBoolean
import com.zegreatrob.coupling.json.toDouble
import com.zegreatrob.coupling.json.toIntFromStringOrInt
import com.zegreatrob.coupling.model.tribe.PairingRule
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.defaultTribe
import com.zegreatrob.coupling.server.action.tribe.SaveTribeCommand
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.command
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlin.js.Json

val saveTribeResolver = dispatch(
    command,
    { _, args -> args.saveTribeInput().toTribe().let(::SaveTribeCommand) },
    { true }
)

private fun Json.toTribe() = Tribe(
    id = TribeId(stringValue("tribeId") ?: ""),
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

private fun Json.saveTribeInput() = this["input"].unsafeCast<Json>()


