package com.zegreatrob.coupling.sdk

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.action.party.DeletePartyCommand
import com.zegreatrob.coupling.action.party.SavePartyCommand
import com.zegreatrob.coupling.action.player.DeletePlayerCommand
import com.zegreatrob.coupling.action.player.SavePlayerCommand
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.stubmodel.stubParties
import com.zegreatrob.coupling.stubmodel.stubParty
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.minassert.assertContains
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minassert.assertIsNotEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.testmints.async.asyncTestTemplate
import kotlin.test.Test
import kotlin.test.assertNotNull

class SdkPartyTest {

    @Test
    fun deleteWillMakePartyInaccessible() = asyncSetup(object {
        val party = stubParty()
    }) {
        sdk().perform(SavePartyCommand(party))
    } exercise {
        with(sdk()) {
            perform(DeletePartyCommand(party.id))
            Pair(
                perform(graphQuery { partyList() })?.partyList,
                perform(graphQuery { party(party.id) { party() } })
                    ?.partyData
                    ?.party?.data,
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
                ?.partyData
                ?.party?.data
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

    private fun List<Record<Party>>.parties() = map(Record<Party>::data)

    private fun List<Party>?.assertContainsAll(expectedParties: List<Party>) {
        assertNotNull(this, "List was null.")
        expectedParties.forEach(this::assertContains)
    }

    private val setupWithPlayerMatchingUserTwoSdks = asyncTestTemplate(
        sharedSetup = suspend {
            object {
                suspend fun altSdk() = altAuthorizedSdkDeferred.await()
                val party = Party(PartyId(uuid4().toString()), name = "party-from-endpoint-tests")
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
        result.map(Record<Party>::data)
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
            ?.partyData
            ?.party
    } verify { result ->
        result?.data.assertIsEqualTo(party)
    }

    @Test
    fun saveWillIncludeModificationInformation() = asyncSetup(object {
        val party = stubParty()
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
}
