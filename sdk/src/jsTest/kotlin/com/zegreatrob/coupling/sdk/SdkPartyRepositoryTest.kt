package com.zegreatrob.coupling.sdk

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.repository.validation.ContextMint
import com.zegreatrob.coupling.repository.validation.MagicClock
import com.zegreatrob.coupling.repository.validation.PartyRepositoryValidator
import com.zegreatrob.coupling.repository.validation.SharedContext
import com.zegreatrob.coupling.repository.validation.SharedContextData
import com.zegreatrob.coupling.repository.validation.bind
import com.zegreatrob.coupling.stubmodel.stubParty
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.coupling.stubmodel.stubUser
import com.zegreatrob.minassert.assertContains
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncTestTemplate
import kotlin.test.Test

private typealias SdkMint = ContextMint<SdkPartyRepository>

class SdkPartyRepositoryTest : PartyRepositoryValidator<SdkPartyRepository> {

    override val repositorySetup = asyncTestTemplate<SharedContext<SdkPartyRepository>>(sharedSetup = {
        val clock = MagicClock()
        val sdk = authorizedSdk()
        SharedContextData(sdk.partyRepository, clock, stubUser().copy(email = primaryAuthorizedUsername))
    })

    private val setupWithPlayerMatchingUserTwoSdks = repositorySetup.extend(sharedSetup = { context ->
        val sdkForOtherUser = altAuthorizedSdkDeferred.await()
        object {
            val repository = context.repository
            val sdkForOtherUser = sdkForOtherUser
            val party = Party(PartyId(uuid4().toString()), name = "tribe-from-endpoint-tests")
            val playerMatchingSdkUser = stubPlayer().copy(email = context.user.email)
        }
    }, sharedTeardown = {
            it.repository.deleteIt(it.party.id)
        })

    @Test
    fun getWillReturnAnyTribeThatHasPlayerWithGivenEmail() = setupWithPlayerMatchingUserTwoSdks {
        sdkForOtherUser.partyRepository.save(party)
        sdkForOtherUser.playerRepository.save(party.id.with(playerMatchingSdkUser))
    } exercise {
        repository.getParties()
    } verify { result ->
        result.map { it.data }
            .assertContains(party)
    }

    @Test
    fun getWillNotReturnTribeIfPlayerHadEmailButThenHadItRemoved() = setupWithPlayerMatchingUserTwoSdks {
        sdkForOtherUser.partyRepository.save(party)
        sdkForOtherUser.playerRepository.save(party.id.with(playerMatchingSdkUser))
        sdkForOtherUser.playerRepository.save(party.id.with(playerMatchingSdkUser.copy(email = "something else")))
    } exercise {
        repository.getParties()
    } verify { result ->
        result.map { it.data }.contains(party)
            .assertIsEqualTo(false)
    }

    @Test
    fun getWillNotReturnTribeIfPlayerHadEmailButPlayerWasRemoved() = setupWithPlayerMatchingUserTwoSdks {
        sdkForOtherUser.partyRepository.save(party)
        sdkForOtherUser.playerRepository.save(party.id.with(playerMatchingSdkUser))
        sdkForOtherUser.playerRepository.deletePlayer(party.id, playerMatchingSdkUser.id)
    } exercise {
        repository.getParties()
    } verify { result ->
        result.map { it.data }.contains(party)
            .assertIsEqualTo(false)
    }

    @Test
    fun saveWillNotSaveWhenTribeAlreadyExistsForSomeoneElse() = setupWithPlayerMatchingUserTwoSdks {
        sdkForOtherUser.partyRepository.save(party)
    } exercise {
        repository.save(party.copy(name = "changed name"))
        sdkForOtherUser.partyRepository.getPartyRecord(party.id)
    } verify { result ->
        result?.data.assertIsEqualTo(party)
    }

    override fun saveWillIncludeModificationInformation() = repositorySetup.with(
        object : SdkMint() {
            val tribe = stubParty()
        }.bind()
    ) {
        repository.save(tribe)
    } exercise {
        repository.getParties()
    } verifyAnd { result ->
        result.first { it.data.id == tribe.id }.apply {
            modifyingUserId.assertIsEqualTo(user.email)
            timestamp.isWithinOneSecondOfNow()
        }
    } teardown {
        repository.deleteIt(tribe.id)
    }
}
