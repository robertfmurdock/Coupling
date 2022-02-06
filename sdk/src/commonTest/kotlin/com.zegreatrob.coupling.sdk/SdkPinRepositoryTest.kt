package com.zegreatrob.coupling.sdk

import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.repository.validation.*
import com.zegreatrob.coupling.stubmodel.stubPin
import com.zegreatrob.coupling.stubmodel.stubTribe
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.AsyncMints.asyncSetup
import com.zegreatrob.testmints.async.AsyncMints.asyncTestTemplate
import kotlin.test.Test

class SdkPinRepositoryTest : PinRepositoryValidator<SdkPinRepository> {

    override val repositorySetup = asyncTestTemplate<SdkTribeContext<SdkPinRepository>>(sharedSetup = {
        val sdk = authorizedSdk()
        val tribe = stubTribe()
        SdkTribeContext(sdk, sdk.pinRepository, tribe.id, MagicClock())
            .apply {
                tribe.save()
            }
    }, sharedTeardown = {
        it.sdk.tribeRepository.delete(it.tribeId)
    })

    @Test
    fun givenNoAuthGetIsNotAllowed() = asyncSetup({
        val sdk = authorizedSdk()
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

fun DateTime.isWithinOneSecondOfNow() {
    val timeSpan = DateTime.now() - this
    (timeSpan.seconds < 1)
        .assertIsEqualTo(true)
}
