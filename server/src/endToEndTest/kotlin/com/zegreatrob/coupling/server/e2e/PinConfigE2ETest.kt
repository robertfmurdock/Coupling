package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.server.e2e.external.protractor.*
import com.zegreatrob.minassert.assertContains
import com.zegreatrob.testmints.async.setupAsync
import com.zegreatrob.testmints.async.testAsync
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.await
import kotlin.random.Random
import kotlin.test.Test

class PinConfigE2ETest {

    private fun testWithTribe(handler: suspend (tribe: Tribe) -> Unit) = testAsync {
        val tribe = tribeProvider.await()
        login.await()

        handler(tribe)
    }

    @Test
    fun whenThePinIsNewAndTheAddButtonIsPressedThePinIsSaved() = testWithTribe { tribe ->
        setupAsync(object {
            val newPinName = "Excellent pin name${randomInt()}"
        }) {
            with(PinConfigPage) {
                tribe.id.goToNewPinConfig()
                nameTextField.performClear()
                nameTextField.performSendKeys(newPinName)
            }
        } exerciseAsync {
            with(PinConfigPage) {
                saveButton.performClick()
                waitForLoad()
            }
        } verifyAsync {
            with(PinConfigPage) {
                pinBag.waitToBePresent()
                pinBag.all(By.className("pin-name"))
                    .map { it.getText() }
                    .await()
                    .toList()
                    .assertContains(newPinName)
            }
        }
    }

    companion object {
        private val sdkProvider by lazy {
            GlobalScope.async { authorizedSdk() }
        }

        private val tribeProvider by lazy {
            GlobalScope.async {
                val sdk = sdkProvider.await()
                Tribe(TribeId("${randomInt()}-test"))
                    .also { sdk.save(it) }
            }
        }

        private val login by lazy {
            GlobalScope.async {
                val sdk = sdkProvider.await()
                TestLogin.login(sdk.userEmail)
            }
        }

        private fun randomInt() = Random.nextInt()

    }
}
