package com.zegreatrob.coupling.cli.party

import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.core.requireObject
import com.zegreatrob.coupling.cli.SdkProvider
import com.zegreatrob.coupling.cli.withSdk
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.sdk.gql.GqlQuery
import com.zegreatrob.coupling.sdk.mapper.toDomain
import com.zegreatrob.coupling.sdk.schema.PartyDetailsQuery
import com.zegreatrob.coupling.model.party.PartyDetails as PartyDetailsModel

internal fun formatPartyDetails(details: PartyDetailsModel): String = """
    Party ID: ${details.id}
    Name: ${details.name}
    Email: ${details.email}
    PairingRule: ${details.pairingRule}
    BadgesEnabled: ${details.badgesEnabled}
    DefaultBadgeName: ${details.defaultBadgeName}
    AlternateBadgeName: ${details.alternateBadgeName}
    CallSignsEnabled: ${details.callSignsEnabled}
    AnimationEnabled: ${details.animationEnabled}
    AnimationSpeed: ${details.animationSpeed}
"""
    .trimIndent()

class PartyDetails(private val sdkProvider: SdkProvider) : SuspendingCliktCommand("details") {

    private val context by requireObject<PartyContext>()
    private val partyId: PartyId by requireObject<PartyId>("partyId")

    override suspend fun run() {
        withSdk(env = context.env, echo = ::echo, sdkProvider = sdkProvider) { sdk ->
            val partyDetails = sdk.fire(GqlQuery(PartyDetailsQuery(partyId)))
                ?.party
                ?.partyDetails
                ?.toDomain()
            if (partyDetails == null) {
                throw CliktError("Party not found.", printError = true)
            }
            echo(formatPartyDetails(partyDetails))
        }
    }
}
