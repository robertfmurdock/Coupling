package com.zegreatrob.coupling.sdk

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.repository.validation.*
import com.zegreatrob.coupling.sdk.SdkPlayerRepositoryTest.Companion.catchAxiosError
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.coupling.stubmodel.stubTribe
import com.zegreatrob.coupling.stubmodel.stubUser
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncTestTemplate
import com.zegreatrob.testmints.async.invoke
import kotlin.test.Test

typealias SdkMint = ContextMint<Sdk>

class SdkTribeRepositoryTest : TribeRepositoryValidator<Sdk> {

    override val repositorySetup = asyncTestTemplate<SharedContext<Sdk>>(sharedSetup = {
        val clock = MagicClock()
        val email = "eT-user-${uuid4()}"
        val sdk = authorizedSdk(username = email)
        SharedContextData(sdk, clock, stubUser().copy(email = "$email._temp"))
    })

    private val setupWithPlayerMatchingUserTwoSdks = repositorySetup.extend(sharedSetup = { context ->
        val sdkForOtherUser = authorizedSdk(username = "eT-other-user-${uuid4()}")
        object {
            val sdk = context.repository
            val sdkForOtherUser = sdkForOtherUser
            val tribe = Tribe(TribeId(uuid4().toString()), name = "tribe-from-endpoint-tests")
            val playerMatchingSdkUser = stubPlayer().copy(email = context.user.email)
        }
    })

    @Test
    fun getWillReturnAnyTribeThatHasPlayerWithGivenEmail() = setupWithPlayerMatchingUserTwoSdks {
        sdkForOtherUser.save(tribe)
        sdkForOtherUser.save(tribe.id.with(playerMatchingSdkUser))
    } exercise {
        sdk.getTribes()
    } verify { result ->
        result.map { it.data }
            .assertIsEqualTo(listOf(tribe))
    }

    @Test
    fun getWillNotReturnTribeIfPlayerHadEmailButThenHadItRemoved() = setupWithPlayerMatchingUserTwoSdks {
        sdkForOtherUser.save(tribe)
        sdkForOtherUser.save(tribe.id.with(playerMatchingSdkUser))
        sdkForOtherUser.save(tribe.id.with(playerMatchingSdkUser.copy(email = "something else")))
    } exercise {
        sdk.getTribes()
    } verify { result ->
        result.assertIsEqualTo(emptyList())
    }

    @Test
    fun getWillNotReturnTribeIfPlayerHadEmailButPlayerWasRemoved() = setupWithPlayerMatchingUserTwoSdks {
        sdkForOtherUser.save(tribe)
        sdkForOtherUser.save(tribe.id.with(playerMatchingSdkUser))
        sdkForOtherUser.deletePlayer(tribe.id, playerMatchingSdkUser.id!!)
    } exercise {
        sdk.getTribes()
    } verify { result ->
        result.assertIsEqualTo(emptyList())
    }

    @Test
    fun postWillFailWhenTribeAlreadyExistsForSomeoneElse() = setupWithPlayerMatchingUserTwoSdks {
        sdkForOtherUser.save(tribe)
    } exercise {
        catchAxiosError { sdk.save(tribe) }
    } verify { result ->
        result["status"].assertIsEqualTo(403)
    }

    override fun saveWillIncludeModificationInformation() = repositorySetup(object : SdkMint() {
        val tribe = stubTribe()
    }.bind()) {
        repository.save(tribe)
    } exercise {
        repository.getTribes()
    } verify { result ->
        result.first { it.data.id == tribe.id }.apply {
            modifyingUserId.assertIsEqualTo(user.email)
            timestamp.isWithinOneSecondOfNow()
        }
    }

}