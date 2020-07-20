package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.client.StubDispatchFunc
import com.zegreatrob.coupling.enzyme.external.ShallowWrapper
import com.zegreatrob.coupling.enzyme.external.shallow
import com.zegreatrob.coupling.model.tribe.PairingRule
import com.zegreatrob.coupling.model.tribe.PairingRule.Companion.toValue
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.minassert.assertContains
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minassert.assertIsNotEqualTo
import com.zegreatrob.minspy.SpyData
import com.zegreatrob.minspy.spyFunction
import com.zegreatrob.testmints.invoke
import com.zegreatrob.testmints.setup
import kotlin.js.json
import kotlin.test.Test

class TribeConfigTest {

    @Test
    fun willDefaultTribeThatIsMissingData(): Unit = setup(object {
        val tribe = Tribe(TribeId("1"), name = "1")
    }) exercise {
        shallow(
            TribeConfig,
            TribeConfigProps(tribe, {}, StubDispatchFunc())
        )
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
    fun whenClickTheSaveButtonWillUseCouplingServiceToSaveTheTribe() = setup(object {
        val tribe = Tribe(
            TribeId("1"),
            name = "1",
            alternateBadgeName = "alt",
            defaultBadgeName = "def",
            email = "email-y",
            pairingRule = PairingRule.PreferDifferentBadge
        )
        val pathSetterSpy = SpyData<String, Unit>()
        val stubDispatchFunc = StubDispatchFunc<TribeConfigDispatcher>()
        val wrapper = shallow(
            TribeConfig,
            TribeConfigProps(tribe, pathSetterSpy::spyFunction, stubDispatchFunc)
        )
    }) exercise {
        wrapper.find<Any>("form")
            .simulate("submit", json("preventDefault" to {}))
        stubDispatchFunc.simulateSuccess<SaveTribeCommand>()
    } verify {
        stubDispatchFunc.commandsDispatched<SaveTribeCommand>()
            .assertIsEqualTo(listOf(SaveTribeCommand(tribe)))

        pathSetterSpy.spyReceivedValues.assertContains("/tribes/")
    }

    @Test
    fun whenTribeIsNewWillSuggestIdAutomaticallyAndWillRetainIt() = setup(object {
        val tribe = Tribe(TribeId(""))
        val stubDispatchFunc = StubDispatchFunc<TribeConfigDispatcher>()
        val wrapper = shallow(TribeConfig, TribeConfigProps(tribe, {}, stubDispatchFunc))
        val automatedTribeId = wrapper.find<Any>("#tribe-id").prop("value")
    }) exercise {
        wrapper.find<Any>("form")
            .simulate("submit", json("preventDefault" to {}))
    } verify {
        stubDispatchFunc.commandsDispatched<SaveTribeCommand>()
            .first()
            .tribe.id.value.run {
                assertIsNotEqualTo("")
                assertIsEqualTo(automatedTribeId)
            }
        wrapper.find<Any>("#tribe-id")
            .prop("value")
            .assertIsEqualTo(automatedTribeId)
    }

}
