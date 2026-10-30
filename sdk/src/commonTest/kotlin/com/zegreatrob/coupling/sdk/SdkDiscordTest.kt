package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.BeginDiscordOAuthCommand
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
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.sdk.gql.GqlQuery
import com.zegreatrob.coupling.sdk.schema.CurrentPairAssignmentsQuery
import com.zegreatrob.coupling.sdk.schema.PartyDetailsQuery
import com.zegreatrob.coupling.stubmodel.stubPartyDetails
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.coupling.stubmodel.uuidString
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.action.ActionCannon
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotools.types.collection.notEmptyListOf
import kotlin.test.Test
import kotlin.uuid.Uuid

class SdkDiscordTest {
    @Test
    fun nonmemberCannotInitiateDiscordInstallation() = asyncSetup(object {
        val party = stubPartyDetails()
    }) {
        altAuthorizedSdkDeferred.await().fire(SavePartyCommand(party)).assertIsEqualTo(VoidResult.Accepted)
        sdk().fire(GqlQuery(PartyDetailsQuery(party.id)))?.party.assertIsEqualTo(null)
    } exercise {
        sdk().fire(BeginDiscordOAuthCommand(party.id, OAUTH_CHALLENGE))
    } verify { result ->
        result.assertIsEqualTo(null)
    }

    @Test
    fun unauthenticatedCallerCannotInitiateDiscordInstallation() = asyncSetup(object {
        val party = stubPartyDetails()
        val anonymousSdk = couplingSdk({ "" }, buildClient(traceId = Uuid.random()))
    }) {
        sdk().fire(SavePartyCommand(party)).assertIsEqualTo(VoidResult.Accepted)
    } exercise {
        anonymousSdk.fire(BeginDiscordOAuthCommand(party.id, OAUTH_CHALLENGE))
    } verify { result ->
        result.assertIsEqualTo(null)
    }

    @Test
    fun invalidCallbacksDoNotConsumeValidFlowOrChangeDestination() = asyncSetup(object {
        val party = stubPartyDetails()
        val otherParty = stubPartyDetails()
        val original = uuidString()
        val replacement = uuidString()
        lateinit var spin: SpinCommand
        lateinit var state: String
    }) {
        val alternate = altAuthorizedSdkDeferred.await()
        spin = sdk().preparePartyForAnnouncements(party)
        sdk().fire(SavePartyCommand(otherParty))
        sdk().grantDiscord("simulated:$original", "unused", party.id).assertIsEqualTo(VoidResult.Accepted)
        sdk().announce(spin).assertDeliveredTo(original)
        // Both identities are members: denial must come from state binding.
        sdk().fire(SavePlayerCommand(party.id, stubPlayer().copy(email = ALT_AUTHORIZED_USER_EMAIL)))
        alternate.fire(GqlQuery(PartyDetailsQuery(party.id)))?.party?.partyDetails?.id.assertIsEqualTo(party.id)
        state = requireNotNull(sdk().fire(BeginDiscordOAuthCommand(party.id, OAUTH_CHALLENGE))).state
    } exercise {
        val alternate = altAuthorizedSdkDeferred.await()
        val attempts = listOf(
            sdk().fire(GrantDiscordAccessCommand("simulated:$replacement", "unused", party.id, state, "b".repeat(64))),
            sdk().fire(GrantDiscordAccessCommand("simulated:$replacement", "unused", party.id, "b".repeat(64), oauthVerifier)),
            sdk().fire(GrantDiscordAccessCommand("simulated:$replacement", "unused", otherParty.id, state, oauthVerifier)),
            alternate.fire(GrantDiscordAccessCommand("simulated:$replacement", "unused", party.id, state, oauthVerifier)),
        )
        val unchanged = sdk().announce(spin)
        val valid = sdk().fire(GrantDiscordAccessCommand("simulated:$replacement", "unused", party.id, state, oauthVerifier))
        Triple(attempts, unchanged, valid to sdk().announce(spin))
    } verify { (attempts, unchanged, completion) ->
        attempts.assertIsEqualTo(List(4) { VoidResult.Rejected })
        unchanged.assertDeliveredTo(original)
        completion.first.assertIsEqualTo(VoidResult.Accepted)
        completion.second.assertDeliveredTo(replacement)
    }

    @Test
    fun independentPendingFlowsCanCompleteForSameParty() = asyncSetup(object {
        val party = stubPartyDetails()
        val firstDestination = uuidString()
        val secondDestination = uuidString()
        lateinit var spin: SpinCommand
        lateinit var firstState: String
        lateinit var secondState: String
    }) {
        spin = sdk().preparePartyForAnnouncements(party)
        firstState = requireNotNull(sdk().fire(BeginDiscordOAuthCommand(party.id, OAUTH_CHALLENGE))).state
        secondState = requireNotNull(sdk().fire(BeginDiscordOAuthCommand(party.id, OAUTH_CHALLENGE))).state
        (firstState != secondState).assertIsEqualTo(true)
    } exercise {
        val first = sdk().fire(GrantDiscordAccessCommand("simulated:$firstDestination", "unused", party.id, firstState, oauthVerifier))
        val firstAnnouncement = sdk().announce(spin)
        val second = sdk().fire(GrantDiscordAccessCommand("simulated:$secondDestination", "unused", party.id, secondState, oauthVerifier))
        (first to firstAnnouncement) to (second to sdk().announce(spin))
    } verify { (first, second) ->
        first.first.assertIsEqualTo(VoidResult.Accepted)
        first.second.assertDeliveredTo(firstDestination)
        second.first.assertIsEqualTo(VoidResult.Accepted)
        second.second.assertDeliveredTo(secondDestination)
    }

    @Test
    fun concurrentCallbacksHaveOneWinnerAndCannotReplay() = asyncSetup(object {
        val party = stubPartyDetails()
        lateinit var spin: SpinCommand
        lateinit var state: String
        val firstDestination = uuidString()
        val secondDestination = uuidString()
    }) {
        spin = sdk().preparePartyForAnnouncements(party)
        state = requireNotNull(sdk().fire(BeginDiscordOAuthCommand(party.id, OAUTH_CHALLENGE))).state
    } exercise {
        val results = coroutineScope {
            val first = async { sdk().fire(GrantDiscordAccessCommand("simulated:$firstDestination", "unused", party.id, state, oauthVerifier)) }
            val second = async { sdk().fire(GrantDiscordAccessCommand("simulated:$secondDestination", "unused", party.id, state, oauthVerifier)) }
            first.await() to second.await()
        }
        val replay = sdk().fire(GrantDiscordAccessCommand("simulated:replay", "unused", party.id, state, oauthVerifier))
        Triple(results, replay, sdk().announce(spin))
    } verify { (results, replay, announcement) ->
        listOf(results.first, results.second).count { it == VoidResult.Accepted }.assertIsEqualTo(1)
        listOf(results.first, results.second).count { it == VoidResult.Rejected }.assertIsEqualTo(1)
        replay.assertIsEqualTo(VoidResult.Rejected)
        announcement.assertDeliveredTo(if (results.first == VoidResult.Accepted) firstDestination else secondDestination)
    }

    @Test
    fun failedCodeExchangeConsumesState() = asyncSetup(object {
        val party = stubPartyDetails()
        lateinit var state: String
        lateinit var spin: SpinCommand
    }) {
        spin = sdk().preparePartyForAnnouncements(party)
        state = requireNotNull(sdk().fire(BeginDiscordOAuthCommand(party.id, OAUTH_CHALLENGE))).state
    } exercise {
        val failed = sdk().fire(GrantDiscordAccessCommand("invalid-code", "unused", party.id, state, oauthVerifier))
        val retried = sdk().fire(GrantDiscordAccessCommand("simulated:retry", "unused", party.id, state, oauthVerifier))
        Triple(failed, retried, sdk().announce(spin).messageId)
    } verify { (failed, retried, messageId) ->
        failed.assertIsEqualTo(VoidResult.Rejected)
        retried.assertIsEqualTo(VoidResult.Rejected)
        messageId.assertIsEqualTo(null)
    }

    @Test
    fun grantWithoutInitiationCannotInstallDiscordIntegration() = asyncSetup(object {
        val party = stubPartyDetails()
        lateinit var spin: SpinCommand
    }) {
        spin = sdk().preparePartyForAnnouncements(party)
        sdk().announce(spin).messageId.assertIsEqualTo(null)
    } exercise {
        val result = sdk().fire(GrantDiscordAccessCommand("simulated:unsolicited", "unused", party.id, "", ""))
        result to sdk().announce(spin)
    } verify { (result, announcement) ->
        result.assertIsEqualTo(VoidResult.Rejected)
        announcement.messageId.assertIsEqualTo(null)
    }

    @Test
    fun authorizedOwnerCanInstallDiscordIntegration() = asyncSetup(object {
        val party = stubPartyDetails()
        val destination = uuidString()
        lateinit var spin: SpinCommand
    }) {
        spin = sdk().preparePartyForAnnouncements(party)
        sdk().announce(spin).messageId.assertIsEqualTo(null)
    } exercise {
        val result = sdk().grantDiscord("simulated:$destination", "simulated-guild-$destination", party.id)
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
        sdk().grantDiscord("simulated:$originalDestination", "simulated-guild-$originalDestination", party.id)
            .assertIsEqualTo(VoidResult.Accepted)
        sdk().announce(spin).assertDeliveredTo(originalDestination)
    } exercise {
        val result = sdk().grantDiscord("simulated:$replacementDestination", "simulated-guild-$replacementDestination", party.id)
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
        victimSdk.grantDiscord("simulated:$victimDestination", "simulated-guild-$victimDestination", victimParty.id)
            .assertIsEqualTo(VoidResult.Accepted)
        victimSdk.announce(victimSpin).assertDeliveredTo(victimDestination)
        sdk().fire(GqlQuery(PartyDetailsQuery(victimParty.id)))
            ?.party
            .assertIsEqualTo(null, "Caller must not have access to the victim party")
    } exercise {
        val result = sdk().fire(GrantDiscordAccessCommand("simulated:$attackerDestination", "simulated-guild-$attackerDestination", victimParty.id, "a".repeat(64), oauthVerifier))
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
        sdk().grantDiscord("simulated:$destination", "simulated-guild-$destination", party.id)
            .assertIsEqualTo(VoidResult.Accepted)
        sdk().announce(spin).assertDeliveredTo(destination)
    } exercise {
        val result = sdk().grantDiscord("invalid-code", "unused", party.id)
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
        sdk().grantDiscord("simulated:$destination", "simulated-guild-$destination", party.id)
            .assertIsEqualTo(VoidResult.Accepted)
        sdk().announce(spin).assertDeliveredTo(destination)
    } exercise {
        val result = anonymousSdk.fire(GrantDiscordAccessCommand("simulated:attacker", "simulated-guild-attacker", party.id, "a".repeat(64), oauthVerifier))
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

private val oauthVerifier = "a".repeat(64)
private const val OAUTH_CHALLENGE = "ffe054fe7ae0cb6dc65c3af9b61d5209f439851db43d0ba5997337df154668eb"

private suspend fun ActionCannon<CouplingSdkDispatcher>.grantDiscord(code: String, guildId: String, partyId: PartyId): VoidResult {
    val state = requireNotNull(fire(BeginDiscordOAuthCommand(partyId, OAUTH_CHALLENGE))).state
    return fire(GrantDiscordAccessCommand(code, guildId, partyId, state, oauthVerifier))
}
