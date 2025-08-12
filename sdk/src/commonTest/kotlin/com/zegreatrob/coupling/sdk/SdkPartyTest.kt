package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.CommandResult
import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.action.party.DeletePartyCommand
import com.zegreatrob.coupling.action.party.SavePartyCommand
import com.zegreatrob.coupling.action.party.SaveSlackIntegrationCommand
import com.zegreatrob.coupling.action.party.fire
import com.zegreatrob.coupling.action.player.DeletePlayerCommand
import com.zegreatrob.coupling.action.player.SavePlayerCommand
import com.zegreatrob.coupling.action.player.fire
import com.zegreatrob.coupling.model.AccessType
import com.zegreatrob.coupling.model.Party
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
import kotlin.uuid.Uuid

class SdkPartyTest {

    @Test
    fun deleteWillMakePartyInaccessible() = asyncSetup(object {
        val party = stubPartyDetails()
    }) {
        sdk().fire(SavePartyCommand(party))
    } exercise {
        with(sdk()) {
            fire(DeletePartyCommand(party.id))
            fire(
                graphQuery {
                    partyList { details() }
                    party(party.id) { details() }
                },
            ).let {
                Pair(
                    it?.partyList?.mapNotNull(Party::details)?.map(Record<PartyDetails>::data),
                    it?.party?.details?.data,
                )
            }
        }
    } verifyAnd { (listResult, getResult) ->
        listResult?.find { it.id == this.party.id }
            .assertIsEqualTo(null)
        getResult.assertIsEqualTo(null)
    } teardown {
        sdk().fire(DeletePartyCommand(party.id))
    }

    @Test
    fun saveMultipleThenGetEachByIdWillReturnSavedParties() = asyncSetup(object {
        val parties = stubParties(3)
    }) {
        parties.forEach { sdk().fire(SavePartyCommand(it)) }
    } exercise {
        parties.map {
            sdk().fire(graphQuery { party(it.id) { details() } })
                ?.party
                ?.details?.data
        }
    } verify { result ->
        result.assertIsEqualTo(this.parties)
    }

    @Test
    fun saveMultipleThenGetListWillReturnSavedParties() = asyncSetup(object {
        val parties = stubParties(3)
        val partyIds = parties.map { it.id }
    }) {
        parties.forEach { sdk().fire(SavePartyCommand(it)) }
    } exercise {
        sdk().fire(graphQuery { partyList { details() } })?.partyList
    } verify { result ->
        result?.parties().assertContainsAll(parties)
        result?.filter { partyIds.contains(it.id) }
            ?.map { it.accessType }
            ?.distinct()
            .assertIsEqualTo(listOf(AccessType.Owner))
    }

    @Test
    fun partyThatHasOwnerAsPlayerOnlyShowsUpOnce() = asyncSetup(object {
        val party = stubPartyDetails()
        val playerMatchingSdkUser = stubPlayer().copy(email = PRIMARY_AUTHORIZED_USER_NAME)
    }) {
        sdk().fire(SavePartyCommand(party))
        sdk().fire(SavePlayerCommand(party.id, playerMatchingSdkUser))
    } exercise {
        sdk().fire(graphQuery { partyList { details() } })?.partyList
    } verify { result ->
        result?.filter { it.id == party.id }
            ?.map { it.accessType }
            ?.distinct()
            .assertIsEqualTo(listOf(AccessType.Owner))
    }

    private fun List<Party>.parties() = mapNotNull { it.details?.data }

    private fun List<PartyDetails>?.assertContainsAll(expectedParties: List<PartyDetails>) {
        assertNotNull(this, "List was null.")
        expectedParties.forEach(this::assertContains)
    }

    private val setupWithPlayerMatchingUserTwoSdks = asyncSetup.extend(
        sharedSetup = { _ ->
            object {
                suspend fun altSdk() = altAuthorizedSdkDeferred.await()
                val party = PartyDetails(PartyId(Uuid.random().toString()), name = "party-from-endpoint-tests")
                val playerMatchingSdkUser = stubPlayer().copy(email = PRIMARY_AUTHORIZED_USER_NAME)
            }
        },
        sharedTeardown = {
            sdk().fire(DeletePartyCommand(it.party.id))
        },
    )

    @Test
    fun getWillReturnAnyPartyThatHasPlayerWithGivenEmail() = setupWithPlayerMatchingUserTwoSdks {
        with(altSdk()) {
            fire(SavePartyCommand(party))
            fire(SavePlayerCommand(party.id, playerMatchingSdkUser))
        }
    } exercise {
        sdk().fire(graphQuery { partyList { details() } })?.partyList ?: emptyList()
    } verify { result ->
        result.map { it.details?.data }
            .assertContains(party)
        result.first { it.details?.data == party }.accessType
            .assertIsEqualTo(AccessType.Player)
    }

    @Test
    fun getWillReturnAnyPartyThatHasPlayerWithGivenEmailInAdditionalSection() = setupWithPlayerMatchingUserTwoSdks {
        with(altSdk()) {
            fire(SavePartyCommand(party))
            fire(SavePlayerCommand(party.id, stubPlayer().copy(additionalEmails = setOf(PRIMARY_AUTHORIZED_USER_NAME))))
        }
    } exercise {
        sdk().fire(graphQuery { partyList { details() } })?.partyList ?: emptyList()
    } verify { result ->
        result.map { it.details?.data }
            .assertContains(party)
    }

    @Test
    fun getWillNotReturnPartyIfPlayerHadEmailButThenHadItRemoved() = setupWithPlayerMatchingUserTwoSdks {
        with(altSdk()) {
            fire(SavePartyCommand(party))
            fire(SavePlayerCommand(party.id, playerMatchingSdkUser))
            fire(SavePlayerCommand(party.id, playerMatchingSdkUser.copy(email = "something else")))
        }
    } exercise {
        sdk().fire(graphQuery { partyList { details() } })?.partyList ?: emptyList()
    } verify { result ->
        result.map { it.details?.data }.contains(party)
            .assertIsEqualTo(false)
    }

    @Test
    fun getWillNotReturnPartyIfPlayerHadEmailButPlayerWasRemoved() = setupWithPlayerMatchingUserTwoSdks {
        with(altSdk()) {
            fire(SavePartyCommand(party))
            fire(SavePlayerCommand(party.id, playerMatchingSdkUser))
            fire(DeletePlayerCommand(party.id, playerMatchingSdkUser.id))
        }
    } exercise {
        sdk().fire(graphQuery { partyList { details() } })?.partyList ?: emptyList()
    } verify { result ->
        result.map { it.details?.data }.contains(party)
            .assertIsEqualTo(false)
    }

    @Test
    fun saveWillNotSaveWhenPartyAlreadyExistsForSomeoneElse() = setupWithPlayerMatchingUserTwoSdks {
        altSdk().fire(SavePartyCommand(party))
    } exercise {
        sdk().fire(SavePartyCommand(party.copy(name = "changed name")))
        altSdk().fire(graphQuery { party(party.id) { details() } })
            ?.party
            ?.details
    } verify { result ->
        result?.data.assertIsEqualTo(party)
    }

    @Test
    fun saveWillIncludeModificationInformation() = asyncSetup(object {
        val party = stubPartyDetails()
    }) {
        sdk().fire(SavePartyCommand(party))
    } exercise {
        sdk().fire(graphQuery { partyList { details() } })?.partyList ?: emptyList()
    } verifyAnd { result ->
        result.first { it.id == party.id }.details!!.apply {
            modifyingUserId.assertIsNotEqualTo(null, "As long as an id exists, we're good.")
            timestamp.isWithinOneSecondOfNow()
        }
    } teardown {
        sdk().fire(DeletePartyCommand(party.id))
    }

    @Test
    fun saveIntegrationCanBeLoaded() = asyncSetup(object {
        val party = stubPartyDetails()
        val slackTeam = uuidString()
        val slackChannel = uuidString()
    }) {
        sdk().fire(SavePartyCommand(party))
    } exercise {
        sdk().fire(SaveSlackIntegrationCommand(partyId = party.id, team = slackTeam, channel = slackChannel))
    } verifyAnd { result ->
        result.assertIsEqualTo(VoidResult.Accepted)
        val queryResult = sdk().fire(graphQuery { party(party.id) { integration() } })
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
        sdk().fire(DeletePartyCommand(party.id))
    }

    @Test
    fun cannotSaveIntegrationForUnauthorizedParty() = asyncSetup(object {
        val party = stubPartyDetails()
        val slackTeam = uuidString()
        val slackChannel = uuidString()
    }) exercise {
        sdk().fire(SaveSlackIntegrationCommand(partyId = party.id, team = slackTeam, channel = slackChannel))
    } verify { result ->
        result.assertIsEqualTo(CommandResult.Unauthorized)
        sdk().fire(graphQuery { party(party.id) { integration() } })
            ?.party
            ?.integration
            .assertIsEqualTo(null)
    }
}
