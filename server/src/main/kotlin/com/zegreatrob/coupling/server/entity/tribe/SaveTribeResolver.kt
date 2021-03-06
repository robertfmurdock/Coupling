package com.zegreatrob.coupling.server.entity.tribe

import com.zegreatrob.coupling.json.SaveTribeInput
import com.zegreatrob.coupling.model.tribe.PairingRule
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.defaultTribe
import com.zegreatrob.coupling.server.action.tribe.SaveTribeCommand
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.command
import com.zegreatrob.coupling.server.graphql.dispatch

val saveTribeResolver = dispatch(command, { _, args: SaveTribeInput -> SaveTribeCommand(args.toModel()) }, { true })

private fun SaveTribeInput.toModel() = Tribe(
    id = TribeId(tribeId),
    name = name,
    email = email,
    pairingRule = PairingRule.fromValue(pairingRule),
    defaultBadgeName = defaultBadgeName ?: defaultTribe.defaultBadgeName,
    alternateBadgeName = alternateBadgeName ?: defaultTribe.alternateBadgeName,
    badgesEnabled = badgesEnabled ?: defaultTribe.badgesEnabled,
    callSignsEnabled = callSignsEnabled ?: defaultTribe.callSignsEnabled,
    animationEnabled = animationsEnabled ?: defaultTribe.animationEnabled,
    animationSpeed = animationSpeed ?: defaultTribe.animationSpeed
)
