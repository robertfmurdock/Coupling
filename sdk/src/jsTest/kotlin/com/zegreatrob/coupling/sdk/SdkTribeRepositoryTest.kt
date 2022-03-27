package com.zegreatrob.coupling.sdk

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.repository.validation.*
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.coupling.stubmodel.stubTribe
import com.zegreatrob.coupling.stubmodel.stubUser
import com.zegreatrob.minassert.assertContains
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncTestTemplate

import kotlin.test.Test

private typealias SdkMint = ContextMint<SdkTribeRepository>

class SdkTribeRepositoryTest : TribeRepositoryValidator<SdkTribeRepository> {

    override val repositorySetup = asyncTestTemplate<SharedContext<SdkTribeRepository>>(sharedSetup = {
        val clock = MagicClock()
        val sdk = authorizedSdk()
        SharedContextData(sdk.tribeRepository, clock, stubUser().copy(email = primaryAuthorizedUsername))
    })

    private val setupWithPlayerMatchingUserTwoSdks = repositorySetup.extend(sharedSetup = { context ->
        val sdkForOtherUser = altAuthorizedSdkDeferred.await()
        object {
            val repository = context.repository
            val sdkForOtherUser = sdkForOtherUser
            val tribe = Tribe(TribeId(uuid4().toString()), name = "tribe-from-endpoint-tests")
            val playerMatchingSdkUser = stubPlayer().copy(email = context.user.email)
        }
    }, sharedTeardown = {
        it.repository.delete(it.tribe.id)
    })

    @Test
    fun getWillReturnAnyTribeThatHasPlayerWithGivenEmail() = setupWithPlayerMatchingUserTwoSdks {
        sdkForOtherUser.tribeRepository.save(tribe)
        sdkForOtherUser.playerRepository.save(tribe.id.with(playerMatchingSdkUser))
    } exercise {
        repository.getTribes()
    } verify { result ->
        result.map { it.data }
            .assertContains(tribe)
    }

    @Test
    fun getWillNotReturnTribeIfPlayerHadEmailButThenHadItRemoved() = setupWithPlayerMatchingUserTwoSdks {
        sdkForOtherUser.tribeRepository.save(tribe)
        sdkForOtherUser.playerRepository.save(tribe.id.with(playerMatchingSdkUser))
        sdkForOtherUser.playerRepository.save(tribe.id.with(playerMatchingSdkUser.copy(email = "something else")))
    } exercise {
        repository.getTribes()
    } verify { result ->
        result.map { it.data }.contains(tribe)
            .assertIsEqualTo(false)
    }

    @Test
    fun getWillNotReturnTribeIfPlayerHadEmailButPlayerWasRemoved() = setupWithPlayerMatchingUserTwoSdks {
        sdkForOtherUser.tribeRepository.save(tribe)
        sdkForOtherUser.playerRepository.save(tribe.id.with(playerMatchingSdkUser))
        sdkForOtherUser.playerRepository.deletePlayer(tribe.id, playerMatchingSdkUser.id)
    } exercise {
        repository.getTribes()
    } verify { result ->
        result.map { it.data }.contains(tribe)
            .assertIsEqualTo(false)
    }

    @Test
    fun saveWillNotSaveWhenTribeAlreadyExistsForSomeoneElse() = setupWithPlayerMatchingUserTwoSdks {
        sdkForOtherUser.tribeRepository.save(tribe)
    } exercise {
        repository.save(tribe.copy(name = "changed name"))
        sdkForOtherUser.tribeRepository.getTribeRecord(tribe.id)
    } verify { result ->
        result?.data.assertIsEqualTo(tribe)
    }

    override fun saveWillIncludeModificationInformation() = repositorySetup.with(object : SdkMint() {
        val tribe = stubTribe()
    }.bind()) {
        repository.save(tribe)
    } exercise {
        repository.getTribes()
    } verifyAnd { result ->
        result.first { it.data.id == tribe.id }.apply {
            modifyingUserId.assertIsEqualTo(user.email)
            timestamp.isWithinOneSecondOfNow()
        }
    } teardown {
        repository.delete(tribe.id)
    }

}
