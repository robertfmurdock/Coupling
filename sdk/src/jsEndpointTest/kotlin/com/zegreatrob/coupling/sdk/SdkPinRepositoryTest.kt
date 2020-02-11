package com.zegreatrob.coupling.sdk

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.pin.TribeIdPin
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.pin.PinRepository
import com.zegreatrob.coupling.repositoryvalidation.PinRepositoryValidator
import com.zegreatrob.coupling.sdk.SdkPlayerRepositoryTest.Companion.catchAxiosError
import com.zegreatrob.minassert.assertContains
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.setupAsync
import com.zegreatrob.testmints.async.testAsync
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import stubTribe
import kotlin.js.Json
import kotlin.test.Test

class SdkPinRepositoryTest : PinRepositoryValidator {

    override suspend fun withRepository(handler: suspend (PinRepository, TribeId) -> Unit) {
        val sdk = authorizedSdk(username = "eT-user-${uuid4()}")
        val tribe = stubTribe()
        sdk.save(tribe)
        handler(sdk, tribe.id)
    }

    @Test
    fun postThenDeleteWillNotShowThatPin() = testAsync {
        val sdk = authorizedSdk(username = "eT-user-${uuid4()}")
        setupAsync(object {
            val tribe = Tribe(TribeId(uuid4().toString()))
            val pins = listOf(
                Pin(monk.id().toString(), "1"),
                Pin(monk.id().toString(), "2"),
                Pin(monk.id().toString(), "3")
            )
        }) {
            coroutineScope {
                sdk.save(tribe)
                pins.forEach { launch { sdk.save(TribeIdPin(tribe.id, it)) } }
            }
        } exerciseAsync {
            sdk.deletePin(tribe.id, pins[1]._id!!)
            sdk.getPins(tribe.id)
        } verifyAsync { result ->
            result.assertContains(pins[0])
                .assertContains(pins[2])
                .size
                .assertIsEqualTo(2)
        }
    }

    @Test
    fun deleteWillFailWhenPinDoesNotExist() = testAsync {
        val sdk = authorizedSdk(username = "eT-user-${uuid4()}")
        setupAsync(object {
            val tribe = Tribe(TribeId(uuid4().toString()))
        }) {
            sdk.save(tribe)
        } exerciseAsync {
            catchAxiosError {
                sdk.deletePin(tribe.id, monk.id().toString())
            }
        } verifyAsync { result ->
            result["status"].assertIsEqualTo(404)
            result["data"].unsafeCast<Json>()["message"]
                .assertIsEqualTo("Pin could not be deleted.")
        }
    }

    @Test
    fun givenNoPinsWillReturnEmptyList() = testAsync {
        val sdk = authorizedSdk()
        setupAsync(object {
            val tribe = Tribe(TribeId(uuid4().toString()))
        }) {
            sdk.save(tribe)
        } exerciseAsync {
            sdk.getPins(tribe.id)
        } verifyAsync { result ->
            result.assertIsEqualTo(emptyList())
        }
    }

    @Test
    fun givenNoAuthGetIsNotAllowed() = testAsync {
        val sdk = authorizedSdk()
        setupAsync(object {}) exerciseAsync {
            sdk.getPins(TribeId("someoneElseTribe"))
        } verifyAsync { result ->
            result.assertIsEqualTo(emptyList())
        }
    }
}