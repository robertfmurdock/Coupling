package com.zegreatrob.coupling.client.tribe

import ShallowWrapper
import com.zegreatrob.coupling.client.DecoratedDispatchFunc
import com.zegreatrob.coupling.client.buildCommandFunc
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.json.toTribe
import com.zegreatrob.coupling.model.tribe.PairingRule
import com.zegreatrob.coupling.model.tribe.PairingRule.Companion.toValue
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.minassert.assertContains
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minspy.SpyData
import com.zegreatrob.testmints.async.ScopeMint
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.testmints.setup
import shallow
import kotlin.js.Json
import kotlin.js.Promise
import kotlin.test.Test

class TribeConfigTest {

    private val styles = useStyles("tribe/TribeConfig")

    @Test
    fun willDefaultTribeThatIsMissingData(): Unit = setup(object {
        val tribe = Tribe(TribeId("1"), name = "1")
    }) exercise {
        shallow(TribeConfig, TribeConfigProps(tribe, {}, DecoratedDispatchFunc { {} }))
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
    fun whenClickTheSaveButtonWillUseCouplingServiceToSaveTheTribe() = asyncSetup(object : ScopeMint() {
        val dispatcher = object : TribeConfigDispatcher {
            override val tribeRepository get() = throw NotImplementedError("Stubbed for testing.")
            val saveSpy = SpyData<Json, Promise<Unit>>()
            override suspend fun Tribe.save() {
                saveSpy.spyFunction(toJson())
            }
        }

        val tribe = Tribe(
            TribeId("1"),
            name = "1",
            alternateBadgeName = "alt",
            defaultBadgeName = "def",
            email = "email-y",
            pairingRule = PairingRule.PreferDifferentBadge
        )

        val pathSetterSpy = SpyData<String, Unit>()
        val commandFunc = dispatcher.buildCommandFunc(exerciseScope)
        val wrapper = shallow(
            TribeConfig,
            TribeConfigProps(tribe, pathSetterSpy::spyFunction, DecoratedDispatchFunc(commandFunc))
        )
    }, {
        dispatcher.saveSpy.spyWillReturn(Promise.resolve(Unit))
        pathSetterSpy.spyWillReturn(Unit)
    }) exercise {
        wrapper.find<Any>(".${styles["saveButton"]}").simulate("click")
    } verify {
        dispatcher.saveSpy.spyReceivedValues.map { it.toTribe() }.assertContains(tribe)
        pathSetterSpy.spyReceivedValues.assertContains("/tribes/")
    }

}