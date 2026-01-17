package com.zegreatrob.coupling.cli.party

import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.core.requireObject
import com.zegreatrob.coupling.cli.loadSdk
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.sdk.CouplingSdkDispatcher
import com.zegreatrob.coupling.sdk.gql.GqlQuery
import com.zegreatrob.coupling.sdk.mapper.toDomain
import com.zegreatrob.coupling.sdk.schema.PartyDetailsQuery
import com.zegreatrob.testmints.action.ActionCannon

class PartyDetails(val cannon: ActionCannon<CouplingSdkDispatcher>?) : SuspendingCliktCommand("details") {

    private val context by requireObject<PartyContext>()
    private val partyId: PartyId by requireObject<PartyId>("partyId")

    override suspend fun run() {
        val actionCannon = cannon ?: loadSdk(context.env, ::echo)
        val partyDetails = actionCannon?.fire(GqlQuery(PartyDetailsQuery(partyId)))
            ?.party?.partyDetails?.toDomain()

        if (partyDetails == null) {
            throw CliktError("Party not found.", printError = true)
        }
        echo("Party ID: ${partyDetails.id}")
        echo("Name: ${partyDetails.name}")
        echo("Email: ${partyDetails.email}")
        echo("PairingRule: ${partyDetails.pairingRule}")
        echo("BadgesEnabled: ${partyDetails.badgesEnabled}")
        echo("DefaultBadgeName: ${partyDetails.defaultBadgeName}")
        echo("AlternateBadgeName: ${partyDetails.alternateBadgeName}")
        echo("CallSignsEnabled: ${partyDetails.callSignsEnabled}")
        echo("AnimationEnabled: ${partyDetails.animationEnabled}")
        echo("AnimationSpeed: ${partyDetails.animationSpeed}")
    }
}
