package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.server.e2e.external.protractor.By
import com.zegreatrob.coupling.server.e2e.external.protractor.waitToBePresent
import com.zegreatrob.minassert.assertContains
import com.zegreatrob.testmints.async.setupAsync
import com.zegreatrob.testmints.async.testAsync
import kotlinx.coroutines.await
import kotlin.random.Random
import kotlin.test.Test

class PinConfigE2ETest {

    @Test
    fun whenThePinIsNewAndTheAddButtonIsPressedThePinIsSaved() = testAsync {
        val sdk = authorizedSdk()
        setupAsync(object {
            val tribe = Tribe(TribeId("${randomInt()}-test"))
            val newPinName = "Excellent pin name${randomInt()}"
        }) {
            sdk.save(tribe)

            TestLogin.login(sdk.userEmail)
            with(PinConfigPage) {
                goToNewPinConfig(tribe.id)
                nameTextField.clear().await()
                nameTextField.sendKeys(newPinName).await()
            }

        } exerciseAsync {
            with(PinConfigPage) {
                saveButton.click();
                wait()
            };
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

    private fun randomInt() = Random.nextInt()
}
