package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.CommandResult
import com.zegreatrob.coupling.action.GrantDiscordAccessCommand
import com.zegreatrob.coupling.action.SpinCommand
import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.action.fire
import com.zegreatrob.coupling.action.party.SavePartyCommand
import com.zegreatrob.coupling.action.party.fire
import com.zegreatrob.coupling.action.player.SavePlayerCommand
import com.zegreatrob.coupling.action.player.fire
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.sdk.gql.GqlQuery
import com.zegreatrob.coupling.sdk.schema.CurrentPairAssignmentsQuery
import com.zegreatrob.coupling.sdk.schema.PartyDetailsQuery
import com.zegreatrob.coupling.stubmodel.stubPartyDetails
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.coupling.stubmodel.uuidString
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.action.ActionCannon
import kotools.types.collection.notEmptyListOf
import kotlin.test.Test
import kotlin.uuid.Uuid

class SdkDiscordTest {
    @Test
    fun authorizedOwnerCanInstallDiscordIntegration() = asyncSetup(object {
        val party = stubPartyDetails()
        val destination = uuidString()
        lateinit var spin: SpinCommand
    }) {
        spin = sdk().preparePartyForAnnouncements(party)
        sdk().announce(spin).messageId.assertIsEqualTo(null)
    } exercise {
        val result = sdk().fire(GrantDiscordAccessCommand("simulated:$destination", "simulated-guild-$destination", party.id))
        result to sdk().announce(spin)
    } verify { (result, announcement) ->
        result.assertIsEqualTo(VoidResult.Accepted)
        announcement.assertDeliveredTo(destination)
    }

    @Test
    fun authorizedOwnerCanReplaceDiscordIntegration() = asyncSetup(object {
        val party = stubPartyDetails()
        val originalDestination = uuidString()
        val replacementDestination = uuidString()
        lateinit var spin: SpinCommand
    }) {
        spin = sdk().preparePartyForAnnouncements(party)
        sdk().fire(GrantDiscordAccessCommand("simulated:$originalDestination", "simulated-guild-$originalDestination", party.id))
            .assertIsEqualTo(VoidResult.Accepted)
        sdk().announce(spin).assertDeliveredTo(originalDestination)
    } exercise {
        val result = sdk().fire(GrantDiscordAccessCommand("simulated:$replacementDestination", "simulated-guild-$replacementDestination", party.id))
        result to sdk().announce(spin)
    } verify { (result, announcement) ->
        result.assertIsEqualTo(VoidResult.Accepted)
        announcement.assertDeliveredTo(replacementDestination)
    }

    @Test
    fun nonmemberCannotRedirectAnotherPartysDiscordAnnouncements() = asyncSetup(object {
        val callerParty = stubPartyDetails()
        val victimParty = stubPartyDetails()
        val victimDestination = uuidString()
        val attackerDestination = uuidString()
        lateinit var victimSpin: SpinCommand
    }) {
        sdk().fire(SavePartyCommand(callerParty))
        val victimSdk = altAuthorizedSdkDeferred.await()
        victimSpin = victimSdk.preparePartyForAnnouncements(victimParty)
        victimSdk.fire(GrantDiscordAccessCommand("simulated:$victimDestination", "simulated-guild-$victimDestination", victimParty.id))
            .assertIsEqualTo(VoidResult.Accepted)
        victimSdk.announce(victimSpin).assertDeliveredTo(victimDestination)
        sdk().fire(GqlQuery(PartyDetailsQuery(victimParty.id)))
            ?.party
            .assertIsEqualTo(null, "Caller must not have access to the victim party")
    } exercise {
        val result = sdk().fire(GrantDiscordAccessCommand("simulated:$attackerDestination", "simulated-guild-$attackerDestination", victimParty.id))
        result to altAuthorizedSdkDeferred.await().announce(victimSpin)
    } verify { (result, announcement) ->
        announcement.assertDeliveredTo(victimDestination)
        result.assertIsEqualTo(CommandResult.Unauthorized)
    }

    @Test
    fun failedExchangePreservesExistingAnnouncementDestination() = asyncSetup(object {
        val party = stubPartyDetails()
        val destination = uuidString()
        lateinit var spin: SpinCommand
    }) {
        spin = sdk().preparePartyForAnnouncements(party)
        sdk().fire(GrantDiscordAccessCommand("simulated:$destination", "simulated-guild-$destination", party.id))
            .assertIsEqualTo(VoidResult.Accepted)
        sdk().announce(spin).assertDeliveredTo(destination)
    } exercise {
        val result = sdk().fire(GrantDiscordAccessCommand("invalid-code", "unused", party.id))
        result to sdk().announce(spin)
    } verify { (result, announcement) ->
        result.assertIsEqualTo(VoidResult.Rejected)
        announcement.assertDeliveredTo(destination)
    }

    @Test
    fun unauthenticatedCallerCannotRedirectDiscordAnnouncements() = asyncSetup(object {
        val party = stubPartyDetails()
        val destination = uuidString()
        val anonymousSdk = couplingSdk({ "" }, buildClient(traceId = Uuid.random()))
        lateinit var spin: SpinCommand
    }) {
        spin = sdk().preparePartyForAnnouncements(party)
        sdk().fire(GrantDiscordAccessCommand("simulated:$destination", "simulated-guild-$destination", party.id))
            .assertIsEqualTo(VoidResult.Accepted)
        sdk().announce(spin).assertDeliveredTo(destination)
    } exercise {
        val result = anonymousSdk.fire(GrantDiscordAccessCommand("simulated:attacker", "simulated-guild-attacker", party.id))
        result to sdk().announce(spin)
    } verify { (result, announcement) ->
        result.assertIsEqualTo(CommandResult.Unauthorized)
        announcement.assertDeliveredTo(destination)
    }
}

private suspend fun ActionCannon<CouplingSdkDispatcher>.preparePartyForAnnouncements(party: PartyDetails): SpinCommand {
    val firstPlayer = stubPlayer()
    val secondPlayer = stubPlayer()
    fire(SavePartyCommand(party))
    fire(SavePlayerCommand(party.id, firstPlayer))
    fire(SavePlayerCommand(party.id, secondPlayer))
    return SpinCommand(party.id, notEmptyListOf(firstPlayer.id, secondPlayer.id), emptyList())
}

private suspend fun ActionCannon<CouplingSdkDispatcher>.announce(spin: SpinCommand) = Announcement(
    result = fire(spin),
    messageId = fire(GqlQuery(CurrentPairAssignmentsQuery(spin.partyId)))
        ?.party
        ?.currentPairingSet
        ?.pairingSetDetails
        ?.discordMessageId,
)

private data class Announcement(val result: SpinCommand.Result, val messageId: String?) {
    fun assertDeliveredTo(destination: String) {
        result.assertIsEqualTo(SpinCommand.Result.Success)
        messageId?.substringBeforeLast('-').assertIsEqualTo("simulated-message-$destination")
    }
}
