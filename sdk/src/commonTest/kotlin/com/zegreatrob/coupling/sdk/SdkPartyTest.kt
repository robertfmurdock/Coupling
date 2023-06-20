package com.zegreatrob.coupling.sdk

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.action.CommandResult
import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.action.party.DeletePartyCommand
import com.zegreatrob.coupling.action.party.SavePartyCommand
import com.zegreatrob.coupling.action.party.SaveSlackIntegrationCommand
import com.zegreatrob.coupling.action.player.DeletePlayerCommand
import com.zegreatrob.coupling.action.player.SavePlayerCommand
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.sdk.gql.graphQuery
import com.zegreatrob.coupling.stubmodel.stubParties
import com.zegreatrob.coupling.stubmodel.stubPartyDetails
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.coupling.stubmodel.uuidString
import com.zegreatrob.minassert.assertContains
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minassert.assertIsNotEqualTo
import kotlin.test.Test
import kotlin.test.assertNotNull

class SdkPartyTest {

    @Test
    fun deleteWillMakePartyInaccessible() = asyncSetup(object {
        val party = stubPartyDetails()
    }) {
        sdk().perform(SavePartyCommand(party))
    } exercise {
        with(sdk()) {
            perform(DeletePartyCommand(party.id))
            Pair(
                perform(graphQuery { partyList() })?.partyList,
                perform(graphQuery { party(party.id) { party() } })
                    ?.party
                    ?.details?.data,
            )
        }
    } verifyAnd { (listResult, getResult) ->
        listResult?.find { it.data.id == this.party.id }
            .assertIsEqualTo(null)
        getResult.assertIsEqualTo(null)
    } teardown {
        sdk().perform(DeletePartyCommand(party.id))
    }

    @Test
    fun saveMultipleThenGetEachByIdWillReturnSavedParties() = asyncSetup(object {
        val parties = stubParties(3)
    }) {
        parties.forEach { sdk().perform(SavePartyCommand(it)) }
    } exercise {
        parties.map {
            sdk().perform(graphQuery { party(it.id) { party() } })
                ?.party
                ?.details?.data
        }
    } verify { result ->
        result.assertIsEqualTo(this.parties)
    }

    @Test
    fun saveMultipleThenGetListWillReturnSavedParties() = asyncSetup(object {
        val parties = stubParties(3)
    }) {
        parties.forEach { sdk().perform(SavePartyCommand(it)) }
    } exercise {
        sdk().perform(graphQuery { partyList() })?.partyList
    } verify { result ->
        result?.parties().assertContainsAll(parties)
    }

    private fun List<Record<PartyDetails>>.parties() = map(Record<PartyDetails>::data)

    private fun List<PartyDetails>?.assertContainsAll(expectedParties: List<PartyDetails>) {
        assertNotNull(this, "List was null.")
        expectedParties.forEach(this::assertContains)
    }

    private val setupWithPlayerMatchingUserTwoSdks = asyncSetup.extend(
        sharedSetup = { _ ->
            object {
                suspend fun altSdk() = altAuthorizedSdkDeferred.await()
                val party = PartyDetails(PartyId(uuid4().toString()), name = "party-from-endpoint-tests")
                val playerMatchingSdkUser = stubPlayer().copy(email = primaryAuthorizedUsername)
            }
        },
        sharedTeardown = {
            sdk().perform(DeletePartyCommand(it.party.id))
        },
    )

    @Test
    fun getWillReturnAnyPartyThatHasPlayerWithGivenEmail() = setupWithPlayerMatchingUserTwoSdks {
        with(altSdk()) {
            perform(SavePartyCommand(party))
            perform(SavePlayerCommand(party.id, playerMatchingSdkUser))
        }
    } exercise {
        sdk().perform(graphQuery { partyList() })?.partyList ?: emptyList()
    } verify { result ->
        result.map(Record<PartyDetails>::data)
            .assertContains(party)
    }

    @Test
    fun getWillNotReturnPartyIfPlayerHadEmailButThenHadItRemoved() = setupWithPlayerMatchingUserTwoSdks {
        with(altSdk()) {
            perform(SavePartyCommand(party))
            perform(SavePlayerCommand(party.id, playerMatchingSdkUser))
            perform(SavePlayerCommand(party.id, playerMatchingSdkUser.copy(email = "something else")))
        }
    } exercise {
        sdk().perform(graphQuery { partyList() })?.partyList ?: emptyList()
    } verify { result ->
        result.map { it.data }.contains(party)
            .assertIsEqualTo(false)
    }

    @Test
    fun getWillNotReturnPartyIfPlayerHadEmailButPlayerWasRemoved() = setupWithPlayerMatchingUserTwoSdks {
        with(altSdk()) {
            perform(SavePartyCommand(party))
            perform(SavePlayerCommand(party.id, playerMatchingSdkUser))
            perform(DeletePlayerCommand(party.id, playerMatchingSdkUser.id))
        }
    } exercise {
        sdk().perform(graphQuery { partyList() })?.partyList ?: emptyList()
    } verify { result ->
        result.map { it.data }.contains(party)
            .assertIsEqualTo(false)
    }

    @Test
    fun saveWillNotSaveWhenPartyAlreadyExistsForSomeoneElse() = setupWithPlayerMatchingUserTwoSdks {
        altSdk().perform(SavePartyCommand(party))
    } exercise {
        sdk().perform(SavePartyCommand(party.copy(name = "changed name")))
        altSdk().perform(graphQuery { party(party.id) { party() } })
            ?.party
            ?.details
    } verify { result ->
        result?.data.assertIsEqualTo(party)
    }

    @Test
    fun saveWillIncludeModificationInformation() = asyncSetup(object {
        val party = stubPartyDetails()
    }) {
        sdk().perform(SavePartyCommand(party))
    } exercise {
        sdk().perform(graphQuery { partyList() })?.partyList ?: emptyList()
    } verifyAnd { result ->
        result.first { it.data.id == party.id }.apply {
            modifyingUserId.assertIsNotEqualTo(null, "As long as an id exists, we're good.")
            timestamp.isWithinOneSecondOfNow()
        }
    } teardown {
        sdk().perform(DeletePartyCommand(party.id))
    }

    @Test
    fun saveIntegrationCanBeLoaded() = asyncSetup(object {
        val party = stubPartyDetails()
        val slackTeam = uuidString()
        val slackChannel = uuidString()
    }) {
        sdk().perform(SavePartyCommand(party))
    } exercise {
        sdk().perform(SaveSlackIntegrationCommand(partyId = party.id, team = slackTeam, channel = slackChannel))
    } verifyAnd { result ->
        result.assertIsEqualTo(VoidResult.Accepted)
        val queryResult = sdk().perform(graphQuery { party(party.id) { integration() } })
            ?.party
            ?.integration
        assertNotNull(queryResult)
        queryResult.apply {
            data.slackTeam.assertIsEqualTo(slackTeam)
            data.slackChannel.assertIsEqualTo(slackChannel)
            modifyingUserId.assertIsNotEqualTo(null, "As long as an id exists, we're good.")
            timestamp.isWithinOneSecondOfNow()
        }
    } teardown {
        sdk().perform(DeletePartyCommand(party.id))
    }

    @Test
    fun cannotSaveIntegrationForUnauthorizedParty() = asyncSetup(object {
        val party = stubPartyDetails()
        val slackTeam = uuidString()
        val slackChannel = uuidString()
    }) exercise {
        sdk().perform(SaveSlackIntegrationCommand(partyId = party.id, team = slackTeam, channel = slackChannel))
    } verify { result ->
        result.assertIsEqualTo(CommandResult.Unauthorized)
        sdk().perform(graphQuery { party(party.id) { integration() } })
            ?.party
            ?.integration
            .assertIsEqualTo(null)
    }
}
