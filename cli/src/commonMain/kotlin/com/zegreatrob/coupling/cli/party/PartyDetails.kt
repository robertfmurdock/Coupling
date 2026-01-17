package com.zegreatrob.coupling.cli.party

import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.core.requireObject
import com.zegreatrob.coupling.cli.withSdk
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.sdk.CouplingSdkDispatcher
import com.zegreatrob.coupling.sdk.gql.GqlQuery
import com.zegreatrob.coupling.sdk.schema.PartyDetailsQuery
import com.zegreatrob.testmints.action.ActionCannon
import kotlinx.coroutines.CoroutineScope

class PartyDetails(val scope: CoroutineScope, val cannon: ActionCannon<CouplingSdkDispatcher>?) : SuspendingCliktCommand("details") {

    private val context by requireObject<PartyContext>()
    private val partyId: PartyId by requireObject<PartyId>("partyId")

    override suspend fun run() {
        withSdk(scope, context.env, ::echo, cannon = cannon) {
            val data = it.fire(GqlQuery(PartyDetailsQuery(partyId)))
            echo("Party ID: ${data?.party?.partyDetails?.id}")
            echo("Name: ${data?.party?.partyDetails?.name}")
            echo("Email: ${data?.party?.partyDetails?.email}")
            echo("PairingRule: ${data?.party?.partyDetails?.pairingRule}")
            echo("BadgesEnabled: ${data?.party?.partyDetails?.badgesEnabled}")
            echo("DefaultBadgeName: ${data?.party?.partyDetails?.defaultBadgeName}")
            echo("AlternateBadgeName: ${data?.party?.partyDetails?.alternateBadgeName}")
            echo("CallSignsEnabled: ${data?.party?.partyDetails?.callSignsEnabled}")
            echo("AnimationsEnabled: ${data?.party?.partyDetails?.animationsEnabled}")
            echo("AnimationSpeed: ${data?.party?.partyDetails?.animationSpeed}")
        }?.join()
    }
}
