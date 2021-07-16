package com.zegreatrob.coupling.server.entity.tribe

import com.zegreatrob.coupling.json.couplingJsonFormat
import com.zegreatrob.coupling.model.tribe.PairingRule
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.defaultTribe
import com.zegreatrob.coupling.server.action.tribe.SaveTribeCommand
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.command
import com.zegreatrob.coupling.server.graphql.dispatch
import com.zegreatrob.minjson.at
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.decodeFromDynamic
import kotlin.js.Json

val saveTribeResolver = dispatch(
    command,
    { _, args -> args.toTribe().let(::SaveTribeCommand) },
    { true }
)

private fun Json.toTribe() = couplingJsonFormat.decodeFromDynamic<SaveTribeInput>(at("/input")).let(::toModel)

private fun toModel(it: SaveTribeInput) = Tribe(
    id = TribeId(it.tribeId),
    name = it.name,
    email = it.email,
    pairingRule = PairingRule.fromValue(it.pairingRule),
    defaultBadgeName = it.defaultBadgeName ?: defaultTribe.defaultBadgeName,
    alternateBadgeName = it.alternateBadgeName ?: defaultTribe.alternateBadgeName,
    badgesEnabled = it.badgesEnabled ?: defaultTribe.badgesEnabled,
    callSignsEnabled = it.callSignsEnabled ?: defaultTribe.callSignsEnabled,
    animationEnabled = it.animationsEnabled ?: defaultTribe.animationEnabled,
    animationSpeed = it.animationSpeed ?: defaultTribe.animationSpeed
)

@Serializable
data class SaveTribeInput(
    val tribeId: String,
    val name: String?,
    val email: String?,
    val pairingRule: Int?,
    val badgesEnabled: Boolean?,
    val defaultBadgeName: String?,
    val alternateBadgeName: String?,
    val callSignsEnabled: Boolean?,
    val animationsEnabled: Boolean?,
    val animationSpeed: Double?,
)