package com.zegreatrob.coupling.sdk

import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.repository.validation.*
import com.zegreatrob.coupling.stubmodel.stubPin
import com.zegreatrob.coupling.stubmodel.stubTribe
import com.zegreatrob.coupling.stubmodel.stubUser
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.AsyncMints.asyncSetup
import com.zegreatrob.testmints.async.AsyncMints.asyncTestTemplate
import kotlin.test.Test

class SdkPinRepositoryTest : PinRepositoryValidator<Sdk> {

    override val repositorySetup = asyncTestTemplate<TribeContext<Sdk>>(sharedSetup = {
        val sdk = authorizedKtorSdk()
        val tribe = stubTribe()
        sdk.save(tribe)
        TribeContextData(sdk, tribe.id, MagicClock(), stubUser().copy(email = primaryAuthorizedUsername))
    }, sharedTeardown = {
        it.repository.delete(it.tribeId)
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
        otherSdk.save(otherTribe)
        otherSdk.save(otherTribe.id.with(stubPin()))
    } exercise {
        sdk.getPins(otherTribe.id)
    } verifyAnd { result ->
        result.assertIsEqualTo(emptyList())
    } teardown {
        otherSdk.delete(otherTribe.id)
    }

    override fun savedPinsIncludeModificationDateAndUsername() = repositorySetup(object : TribeContextMint<Sdk>() {
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
