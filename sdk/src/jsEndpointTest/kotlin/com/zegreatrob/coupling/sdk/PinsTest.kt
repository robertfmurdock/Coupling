package com.zegreatrob.coupling.sdk

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.pin.TribeIdPin
import com.zegreatrob.coupling.model.tribe.KtTribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.sdk.PlayersTest.Companion.catchError
import com.zegreatrob.minassert.assertContains
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.setupAsync
import com.zegreatrob.testmints.async.testAsync
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.js.Json
import kotlin.test.Test

class PinsTest {

    @Test
    fun postThenGetWillShowAllPins() = testAsync {
        val sdk = authorizedSdk(username = "eT-user-${uuid4()}")
        setupAsync(object {
            val tribe = KtTribe(TribeId(uuid4().toString()))
            val pins = listOf(
                Pin(uuid4().toString(), "1", tribe.id.value),
                Pin(uuid4().toString(), "2", tribe.id.value),
                Pin(uuid4().toString(), "3", tribe.id.value)
            )
        }) {
            sdk.save(tribe)
            pins.forEach { sdk.save(TribeIdPin(tribe.id, it)) }
        } exerciseAsync {
            sdk.getPins(tribe.id)
        } verifyAsync { result ->
            result.assertIsEqualTo(pins)
        }
    }

    @Test
    fun postThenDeleteWillNotShowThatPin() = testAsync {
        val sdk = authorizedSdk(username = "eT-user-${uuid4()}")
        setupAsync(object {
            val tribe = KtTribe(TribeId(uuid4().toString()))
            val pins = listOf(
                Pin(monk.id().toString(), "1", tribe.id.value),
                Pin(monk.id().toString(), "2", tribe.id.value),
                Pin(monk.id().toString(), "3", tribe.id.value)
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
            val tribe = KtTribe(TribeId(uuid4().toString()))
        }) {
            sdk.save(tribe)
        } exerciseAsync {
            catchError {
                sdk.deletePin(tribe.id, monk.id().toString())
            }
        } verifyAsync { result ->
            result["status"].assertIsEqualTo(404)
            result["data"].unsafeCast<Json>()["message"]
                .assertIsEqualTo("Pin could not be deleted.")
        }
    }

    @Test
    fun givenNoAuthGetIsNotAllowed() = testAsync {
        val sdk = authorizedSdk()
        setupAsync(object {}) exerciseAsync {
            catchError {
                sdk.getPins(TribeId("someoneElseTribe"))
            }
        } verifyAsync { result ->
            result["status"].assertIsEqualTo(404)
        }
    }
}