package com.zegreatrob.coupling.server.entity.tribe

import com.zegreatrob.coupling.model.tribe.PairingRule
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.defaultTribe
import com.zegreatrob.coupling.server.action.tribe.SaveTribeCommand
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.command
import com.zegreatrob.coupling.server.graphql.dispatch
import com.zegreatrob.minjson.at
import kotlin.js.Json

val saveTribeResolver = dispatch(
    command,
    { _, args -> args.toTribe().let(::SaveTribeCommand) },
    { true }
)

private fun Json.toTribe() = Tribe(
    id = TribeId(at("/input/tribeId")!!),
    name = at("/input/name"),
    email = at("/input/email"),
    pairingRule = PairingRule.fromValue(at("/input/pairingRule")),
    defaultBadgeName = at("/input/defaultBadgeName") ?: defaultTribe.defaultBadgeName,
    alternateBadgeName = at("/input/alternateBadgeName") ?: defaultTribe.alternateBadgeName,
    badgesEnabled = at("/input/badgesEnabled") ?: defaultTribe.badgesEnabled,
    callSignsEnabled = at("/input/callSignsEnabled") ?: defaultTribe.callSignsEnabled,
    animationEnabled = at("/input/animationsEnabled") ?: defaultTribe.animationEnabled,
    animationSpeed = at("/input/animationSpeed") ?: defaultTribe.animationSpeed
)
