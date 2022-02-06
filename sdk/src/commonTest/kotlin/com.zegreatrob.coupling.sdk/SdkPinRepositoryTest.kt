package com.zegreatrob.coupling.sdk

import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.repository.validation.*
import com.zegreatrob.coupling.stubmodel.stubPin
import com.zegreatrob.coupling.stubmodel.stubTribe
import com.zegreatrob.coupling.stubmodel.stubUser
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.AsyncMints.asyncSetup
import com.zegreatrob.testmints.async.AsyncMints.asyncTestTemplate
import kotlin.test.Test

class SdkPinRepositoryTest : PinRepositoryValidator<SdkPinRepository> {

    override val repositorySetup = asyncTestTemplate<SdkTribeContext<SdkPinRepository>>(sharedSetup = {
        val sdk = authorizedKtorSdk()
        val tribe = stubTribe()
        sdk.tribeRepository.save(tribe)

        SdkTribeContext(sdk, sdk.pinRepository, tribe.id, MagicClock())
    }, sharedTeardown = {
        it.sdk.tribeRepository.delete(it.tribeId)
    })

    @Test
    fun givenNoAuthGetIsNotAllowed() = asyncSetup({
        val sdk = authorizedKtorSdk()
        val otherSdk = altAuthorizedSdkDeferred.await()
        object {
            val otherTribe = stubTribe()
            val sdk = sdk
            val otherSdk = otherSdk
        }
    }) {
        otherSdk.tribeRepository.save(otherTribe)
        otherSdk.pinRepository.save(otherTribe.id.with(stubPin()))
    } exercise {
        sdk.pinRepository.getPins(otherTribe.id)
    } verifyAnd { result ->
        result.assertIsEqualTo(emptyList())
    } teardown {
        otherSdk.tribeRepository.delete(otherTribe.id)
    }

    override fun savedPinsIncludeModificationDateAndUsername() = repositorySetup(object : TribeContextMint<SdkPinRepository>() {
        val pin = stubPin()
    }.bind()) {
        repository.save(tribeId.with(pin))
    } exercise {
        repository.getPins(tribeId)
    } verify { result ->
        result.size.assertIsEqualTo(1)
        result.first().apply {
            modifyingUserId.assertIsEqualTo(user.email)
            timestamp.isWithinOneSecondOfNow()
        }
    }

}

class SdkTribeContext<T>(
    val sdk: Sdk,
    override val repository: T,
    override val tribeId: TribeId,
    override val clock: MagicClock
) : TribeContext<T> {
    override val user = stubUser().copy(email = primaryAuthorizedUsername)
}

fun DateTime.isWithinOneSecondOfNow() {
    val timeSpan = DateTime.now() - this
    (timeSpan.seconds < 1)
        .assertIsEqualTo(true)
}
