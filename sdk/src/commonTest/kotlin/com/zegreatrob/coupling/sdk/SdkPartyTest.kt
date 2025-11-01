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
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.sdk.gql.ApolloGraphQuery
import com.zegreatrob.coupling.sdk.schema.PartyAccessTypeAndListQuery
import com.zegreatrob.coupling.sdk.schema.PartyAccessTypeDetailsListQuery
import com.zegreatrob.coupling.sdk.schema.PartyDetailsAndListQuery
import com.zegreatrob.coupling.sdk.schema.PartyDetailsQuery
import com.zegreatrob.coupling.sdk.schema.PartyIntegrationDataQuery
import com.zegreatrob.coupling.sdk.schema.PartyListModificationDataQuery
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
            sdk().fire(ApolloGraphQuery(PartyDetailsAndListQuery(party.id))).let {
                Pair(
                    it?.partyList?.mapNotNull { party -> party.details?.partyDetailsFragment?.toModel() },
                    it?.party?.details?.partyDetailsFragment?.toModel(),
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
            sdk().fire(ApolloGraphQuery(PartyDetailsQuery(it.id)))
                ?.party
                ?.details
                ?.partyDetailsFragment
                ?.toModel()
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
        sdk().fire(ApolloGraphQuery(PartyAccessTypeDetailsListQuery()))
    } verify { result ->
        result?.partyList?.mapNotNull { it.details?.partyDetailsFragment?.toModel() }
            .assertContainsAll(parties)
        result?.partyList?.filter { partyIds.contains(it.id) }
            ?.map { it.accessType.toModel() }
            ?.distinct()
            .assertIsEqualTo(listOf(AccessType.Owner))
    }

    @Test
    fun partyThatHasOwnerAsPlayerOnlyShowsUpOnceInList() = asyncSetup(object {
        val party = stubPartyDetails()
        val playerMatchingSdkUser = stubPlayer().copy(email = PRIMARY_AUTHORIZED_USER_EMAIL)
    }) {
        sdk().fire(SavePartyCommand(party))
        sdk().fire(SavePlayerCommand(party.id, playerMatchingSdkUser))
    } exercise {
        sdk().fire(ApolloGraphQuery(PartyAccessTypeAndListQuery(party.id)))
    } verify { result ->
        result?.partyList
            ?.filter { it.id == party.id }
            ?.map { it.accessType.toModel() }
            ?.distinct()
            .assertIsEqualTo(listOf(AccessType.Owner))
        result?.party?.accessType?.toModel()
            .assertIsEqualTo(AccessType.Owner)
    }

    private fun List<PartyDetails>?.assertContainsAll(expectedParties: List<PartyDetails>) {
        assertNotNull(this, "List was null.")
        expectedParties.forEach(this::assertContains)
    }

    private val setupWithPlayerMatchingUserTwoSdks = asyncSetup.extend(
        sharedSetup = { _ ->
            object {
                suspend fun altSdk() = altAuthorizedSdkDeferred.await()
                val party = PartyDetails(PartyId(Uuid.random().toString()), name = "party-from-endpoint-tests")
                val playerMatchingSdkUser = stubPlayer().copy(email = PRIMARY_AUTHORIZED_USER_EMAIL)
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
        sdk().fire(ApolloGraphQuery(PartyAccessTypeDetailsListQuery()))
            ?.partyList
    } verify { result ->
        result?.map { it.details?.partyDetailsFragment?.toModel() }
            .assertContains(party)
        result?.first { it.id == party.id }
            ?.accessType
            ?.toModel()
            .assertIsEqualTo(AccessType.Player)
    }

    @Test
    fun getWillReturnAnyPartyThatHasPlayerWithGivenEmailInAdditionalSection() = setupWithPlayerMatchingUserTwoSdks {
        with(altSdk()) {
            fire(SavePartyCommand(party))
            fire(
                SavePlayerCommand(
                    party.id,
                    stubPlayer().copy(additionalEmails = setOf(PRIMARY_AUTHORIZED_USER_EMAIL)),
                ),
            )
        }
    } exercise {
        sdk().fire(ApolloGraphQuery(PartyAccessTypeDetailsListQuery()))
            ?.partyList
    } verify { result ->
        result?.map { it.details?.partyDetailsFragment?.toModel() }
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
        sdk().fire(ApolloGraphQuery(PartyAccessTypeDetailsListQuery()))
            ?.partyList
    } verify { result ->
        result?.map { it.details?.partyDetailsFragment?.toModel() }?.contains(party)
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
        sdk().fire(ApolloGraphQuery(PartyAccessTypeDetailsListQuery()))
            ?.partyList
    } verify { result ->
        result?.map { it.details?.partyDetailsFragment?.toModel() }?.contains(party)
            .assertIsEqualTo(false)
    }

    @Test
    fun saveWillNotSaveWhenPartyAlreadyExistsForSomeoneElse() = setupWithPlayerMatchingUserTwoSdks {
        altSdk().fire(SavePartyCommand(party))
    } exercise {
        sdk().fire(SavePartyCommand(party.copy(name = "changed name")))
        altSdk().fire(ApolloGraphQuery(PartyDetailsQuery(party.id)))
            ?.party
            ?.details
            ?.partyDetailsFragment
            ?.toModel()
    } verify { result ->
        result.assertIsEqualTo(party)
    }

    @Test
    fun saveWillIncludeModificationInformation() = asyncSetup(object {
        val party = stubPartyDetails()
    }) {
        sdk().fire(SavePartyCommand(party))
    } exercise {
        sdk().fire(ApolloGraphQuery(PartyListModificationDataQuery()))
            ?.partyList
    } verifyAnd { partyList ->
        partyList?.first { it.id == party.id }
            ?.details!!
            .apply {
                modifyingUserEmail.assertIsNotEqualTo(null, "As long as an id exists, we're good.")
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

        val integration = sdk().fire(ApolloGraphQuery(PartyIntegrationDataQuery(party.id)))
            ?.party
            ?.integration
        assertNotNull(integration)
        integration.slackTeam.assertIsEqualTo(slackTeam)
        integration.slackChannel.assertIsEqualTo(slackChannel)
        integration.modifyingUserEmail.assertIsNotEqualTo(null, "As long as an id exists, we're good.")
        integration.timestamp.isWithinOneSecondOfNow()
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
        sdk().fire(ApolloGraphQuery(PartyIntegrationDataQuery(party.id)))
            ?.party
            ?.integration
            .assertIsEqualTo(null)
    }
}
