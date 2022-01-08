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
import com.zegreatrob.testmints.async.invoke
import kotlin.test.Test

typealias SdkMint = ContextMint<Sdk>

class SdkTribeRepositoryTest : TribeRepositoryValidator<Sdk> {

    override val repositorySetup = asyncTestTemplate<SharedContext<Sdk>>(sharedSetup = {
        val clock = MagicClock()
        val email = "eT-user-${uuid4()}"
        val sdk = authorizedKtorSdk(username = email)
        SharedContextData(sdk, clock, stubUser().copy(email = "$email._temp"))
    })

    private val setupWithPlayerMatchingUserTwoSdks = repositorySetup.extend(sharedSetup = { context ->
        val sdkForOtherUser = authorizedKtorSdk(username = "eT-other-user-${uuid4()}")
        object {
            val sdk = context.repository
            val sdkForOtherUser = sdkForOtherUser
            val tribe = Tribe(TribeId(uuid4().toString()), name = "tribe-from-endpoint-tests")
            val playerMatchingSdkUser = stubPlayer().copy(email = context.user.email)
        }
    }, sharedTeardown = {
        it.sdk.delete(it.tribe.id)
    })

    @Test
    fun getWillReturnAnyTribeThatHasPlayerWithGivenEmail() = setupWithPlayerMatchingUserTwoSdks {
        sdkForOtherUser.save(tribe)
        sdkForOtherUser.save(tribe.id.with(playerMatchingSdkUser))
    } exercise {
        sdk.getTribes()
    } verify { result ->
        result.map { it.data }
            .assertContains(tribe)
    }

    @Test
    fun getWillNotReturnTribeIfPlayerHadEmailButThenHadItRemoved() = setupWithPlayerMatchingUserTwoSdks {
        sdkForOtherUser.save(tribe)
        sdkForOtherUser.save(tribe.id.with(playerMatchingSdkUser))
        sdkForOtherUser.save(tribe.id.with(playerMatchingSdkUser.copy(email = "something else")))
    } exercise {
        sdk.getTribes()
    } verify { result ->
        result.map { it.data }.contains(tribe)
            .assertIsEqualTo(false)
    }

    @Test
    fun getWillNotReturnTribeIfPlayerHadEmailButPlayerWasRemoved() = setupWithPlayerMatchingUserTwoSdks {
        sdkForOtherUser.save(tribe)
        sdkForOtherUser.save(tribe.id.with(playerMatchingSdkUser))
        sdkForOtherUser.deletePlayer(tribe.id, playerMatchingSdkUser.id)
    } exercise {
        sdk.getTribes()
    } verify { result ->
        result.map { it.data }.contains(tribe)
            .assertIsEqualTo(false)
    }

    @Test
    fun saveWillNotSaveWhenTribeAlreadyExistsForSomeoneElse() = setupWithPlayerMatchingUserTwoSdks {
        sdkForOtherUser.save(tribe)
    } exercise {
        sdk.save(tribe.copy(name = "changed name"))
        sdkForOtherUser.getTribeRecord(tribe.id)
    } verify { result ->
        result?.data.assertIsEqualTo(tribe)
    }

    override fun saveWillIncludeModificationInformation() = repositorySetup(object : SdkMint() {
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
