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
import com.zegreatrob.testmints.async.asyncTestTemplate
import kotlin.test.Test

class SdkPartyTest {

    private val repositorySetup = asyncTestTemplate(sharedSetup = {
        val sdk = authorizedSdk()
        object : BarebonesSdk by sdk {}
    })

    private val setupWithPlayerMatchingUserTwoSdks = repositorySetup.extend(sharedSetup = { context ->
        val sdkForOtherUser = altAuthorizedSdkDeferred.await()
        object {
            val sdk = context
            val sdkForOtherUser = sdkForOtherUser
            val party = Party(PartyId(uuid4().toString()), name = "party-from-endpoint-tests")
            val playerMatchingSdkUser = stubPlayer().copy(email = primaryAuthorizedUsername)
        }
    }, sharedTeardown = {
        it.sdk.perform(DeletePartyCommand(it.party.id))
    })

    @Test
    fun deleteWillMakePartyInaccessible() = repositorySetup.with({
        object : BarebonesSdk by it {
            val party = stubParty()
        }
    }) {
        perform(SavePartyCommand(party))
    } exercise {
        perform(DeletePartyCommand(party.id))
        Pair(
            partyRepository.getParties(),
            partyRepository.getPartyRecord(this.party.id)?.data,
        )
    } verifyAnd { (listResult, getResult) ->
        listResult.find { it.data.id == this.party.id }
            .assertIsEqualTo(null)
        getResult.assertIsEqualTo(null)
    } teardown {
        perform(DeletePartyCommand(party.id))
    }

    @Test
    fun saveMultipleThenGetEachByIdWillReturnSavedParties() = repositorySetup.with(
        {
            object : BarebonesSdk by it {
                val parties = stubParties(3)
            }
        },
    ) {
        parties.forEach { perform(SavePartyCommand(it)) }
    } exercise {
        parties.map { partyRepository.getPartyRecord(it.id)?.data }
    } verify { result ->
        result.assertIsEqualTo(this.parties)
    }

    @Test
    fun saveMultipleThenGetListWillReturnSavedParties() = repositorySetup.with(
        {
            object : BarebonesSdk by it {
                val parties = stubParties(3)
            }
        },
    ) {
        parties.forEach { perform(SavePartyCommand(it)) }
    } exercise {
        partyRepository.getParties()
    } verify { result ->
        result.parties().assertContainsAll(parties)
    }

    private fun List<Record<Party>>.parties() = map(Record<Party>::data)

    private fun List<Party>.assertContainsAll(expectedParties: List<Party>) =
        expectedParties.forEach(this::assertContains)

    @Test
    fun getWillReturnAnyPartyThatHasPlayerWithGivenEmail() = setupWithPlayerMatchingUserTwoSdks {
        with(sdkForOtherUser) {
            perform(SavePartyCommand(party))
            perform(SavePlayerCommand(party.id, playerMatchingSdkUser))
        }
    } exercise {
        sdk.partyRepository.getParties()
    } verify { result ->
        result.map { it.data }
            .assertContains(party)
    }

    @Test
    fun getWillNotReturnPartyIfPlayerHadEmailButThenHadItRemoved() = setupWithPlayerMatchingUserTwoSdks {
        with(sdkForOtherUser) {
            perform(SavePartyCommand(party))
            perform(SavePlayerCommand(party.id, playerMatchingSdkUser))
            perform(SavePlayerCommand(party.id, playerMatchingSdkUser.copy(email = "something else")))
        }
    } exercise {
        sdk.partyRepository.getParties()
    } verify { result ->
        result.map { it.data }.contains(party)
            .assertIsEqualTo(false)
    }

    @Test
    fun getWillNotReturnPartyIfPlayerHadEmailButPlayerWasRemoved() = setupWithPlayerMatchingUserTwoSdks {
        with(sdkForOtherUser) {
            perform(SavePartyCommand(party))
            perform(SavePlayerCommand(party.id, playerMatchingSdkUser))
            perform(DeletePlayerCommand(party.id, playerMatchingSdkUser.id))
        }
    } exercise {
        sdk.partyRepository.getParties()
    } verify { result ->
        result.map { it.data }.contains(party)
            .assertIsEqualTo(false)
    }

    @Test
    fun saveWillNotSaveWhenPartyAlreadyExistsForSomeoneElse() = setupWithPlayerMatchingUserTwoSdks {
        sdkForOtherUser.perform(SavePartyCommand(party))
    } exercise {
        sdk.perform(SavePartyCommand(party.copy(name = "changed name")))
        sdkForOtherUser.partyRepository.getPartyRecord(party.id)
    } verify { result ->
        result?.data.assertIsEqualTo(party)
    }

    @Test
    fun saveWillIncludeModificationInformation() = repositorySetup.with({
        object : BarebonesSdk by it {
            val party = stubParty()
        }
    }) {
        perform(SavePartyCommand(party))
    } exercise {
        partyRepository.getParties()
    } verifyAnd { result ->
        result.first { it.data.id == party.id }.apply {
            modifyingUserId.assertIsNotEqualTo(null, "As long as an id exists, we're good.")
            timestamp.isWithinOneSecondOfNow()
        }
    } teardown {
        perform(DeletePartyCommand(party.id))
    }
}
