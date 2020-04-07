package com.zegreatrob.coupling.sdk

import com.benasher44.uuid.uuid4
import com.soywiz.klock.DateTime
import com.soywiz.klock.TimeProvider
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.repository.pin.PinRepository
import com.zegreatrob.coupling.repository.validation.PinRepositoryValidator
import com.zegreatrob.coupling.stubmodel.stubPin
import com.zegreatrob.coupling.stubmodel.stubTribe
import com.zegreatrob.coupling.stubmodel.stubUser
import com.zegreatrob.coupling.stubmodel.uuidString
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.setupAsync
import com.zegreatrob.testmints.async.testAsync
import kotlin.test.Test

class SdkPinRepositoryTest : PinRepositoryValidator {

    override suspend fun withRepository(clock: TimeProvider, handler: suspend (PinRepository, TribeId, User) -> Unit) {
        val username = "eT-user-${uuid4()}"
        val sdk = authorizedSdk(username = username)
        val tribe = stubTribe()
        sdk.save(tribe)
        handler(sdk, tribe.id, stubUser().copy(email = username))
    }

    @Test
    fun givenNoAuthGetIsNotAllowed() = testAsync {
        val sdk = authorizedSdk()
        val otherSdk = authorizedSdk(uuidString())
        setupAsync(object {
            val otherTribe = stubTribe()
        }) {
            otherSdk.save(otherTribe)
            otherSdk.save(otherTribe.id.with(stubPin()))
        } exerciseAsync {
            sdk.getPins(otherTribe.id)
        } verifyAsync { result ->
            result.assertIsEqualTo(emptyList())
        }
    }

    override fun savedPinsIncludeModificationDateAndUsername() = testRepository { repository, tribeId, user, _ ->
        setupAsync(object {
            val pin = stubPin()
        }) {
            repository.save(tribeId.with(pin))
        } exerciseAsync {
            repository.getPins(tribeId)
        } verifyAsync { result ->
            result.size.assertIsEqualTo(1)
            result.first().apply {
                modifyingUserId.assertIsEqualTo(user.email)
                timestamp.isWithinOneSecondOfNow()
            }
        }
    }

}

fun DateTime.isWithinOneSecondOfNow() {
    val timeSpan = DateTime.now() - this
    (timeSpan.seconds < 1)
        .assertIsEqualTo(true)
}
