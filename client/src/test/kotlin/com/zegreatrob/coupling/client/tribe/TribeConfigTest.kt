package com.zegreatrob.coupling.client.tribe

import ShallowWrapper
import Spy
import SpyData
import com.zegreatrob.coupling.client.loadStyles
import com.zegreatrob.coupling.common.entity.tribe.KtTribe
import com.zegreatrob.coupling.common.entity.tribe.PairingRule
import com.zegreatrob.coupling.common.entity.tribe.PairingRule.Companion.toValue
import com.zegreatrob.coupling.common.entity.tribe.TribeId
import com.zegreatrob.coupling.common.toJson
import com.zegreatrob.coupling.common.toTribe
import com.zegreatrob.minassert.assertContains
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.setupAsync
import com.zegreatrob.testmints.async.testAsync
import com.zegreatrob.testmints.setup
import kotlinx.coroutines.asDeferred
import kotlinx.coroutines.withContext
import shallow
import kotlin.js.Json
import kotlin.js.Promise
import kotlin.test.Test

class TribeConfigTest {

    private val styles = loadStyles<TribeConfigStyles>("tribe/TribeConfig")

    @Test
    fun willDefaultTribeThatIsMissingData(): Unit = setup(object : TribeConfigBuilder {
        val tribe = KtTribe(TribeId("1"), name = "1")

    }) exercise {
        shallow(TribeConfigProps(tribe, {}))
    } verify { wrapper ->
        wrapper.assertHasStandardPairingRule()
                .assertHasDefaultBadgeName()
                .assertHasAlternateBadgeName()
    }

    private fun ShallowWrapper<dynamic>.assertHasAlternateBadgeName() = also {
        find<Any>("#alt-badge-name")
                .prop("value")
                .assertIsEqualTo("Alternate")
    }

    private fun ShallowWrapper<dynamic>.assertHasStandardPairingRule() = also {
        console.log(debug())
        find<Any>("#pairing-rule")
                .prop("value")
                .unsafeCast<Array<String>>()
                .joinToString("")
                .assertIsEqualTo("${toValue(PairingRule.LongestTime)}")
    }

    private fun ShallowWrapper<dynamic>.assertHasDefaultBadgeName() = also {
        find<Any>("#default-badge-name")
                .prop("value")
                .assertIsEqualTo("Default")
    }


    @Test
    fun whenClickTheSaveButtonWillUseCouplingServiceToSaveTheTribe() = testAsync {
        withContext(coroutineContext) {
            setupAsync(object : TribeConfigBuilder {
                override fun buildScope() = this@withContext
                val saveSpy = object : Spy<Json, Promise<Unit>> by SpyData() {}
                override fun KtTribe.saveAsync() = saveSpy.spyFunction(toJson()).asDeferred()

                val tribe = KtTribe(
                        TribeId("1"),
                        name = "1",
                        alternateBadgeName = "alt",
                        defaultBadgeName = "def",
                        email = "email-y",
                        pairingRule = PairingRule.PreferDifferentBadge
                )

                val pathSetterSpy = object : Spy<String, Unit> by SpyData() {}
                val wrapper = shallow(TribeConfigProps(tribe, pathSetterSpy::spyFunction))
            }) {
                saveSpy.spyWillReturn(Promise.resolve(Unit))
                pathSetterSpy.spyWillReturn(Unit)
            } exerciseAsync {
                wrapper.find<Any>(".${styles.saveButton}").simulate("click")
            }
        } verifyAsync {
            saveSpy.spyReceivedValues.map { it.toTribe() }.assertContains(tribe)
            pathSetterSpy.spyReceivedValues.assertContains("/tribes/")
        }
    }

}