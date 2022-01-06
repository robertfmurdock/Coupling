package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.client.ConfigForm
import com.zegreatrob.coupling.client.StubDispatchFunc
import com.zegreatrob.coupling.model.tribe.PairingRule
import com.zegreatrob.coupling.model.tribe.PairingRule.Companion.toValue
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minassert.assertIsNotEqualTo
import com.zegreatrob.minenzyme.ShallowWrapper
import com.zegreatrob.minenzyme.shallow
import com.zegreatrob.testmints.setup
import react.router.Navigate
import kotlin.test.Test

class TribeConfigTest {

    @Test
    fun willDefaultTribeThatIsMissingData(): Unit = setup(object {
        val tribe = Tribe(TribeId("1"), name = "1")
    }) exercise {
        shallow(TribeConfig(tribe, StubDispatchFunc()))
            .find(tribeConfigContent)
            .shallow()
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
        val stubDispatchFunc = StubDispatchFunc<TribeConfigDispatcher>()
        val wrapper = shallow(TribeConfig(tribe, stubDispatchFunc))
    }) exercise {
        wrapper.find(tribeConfigContent)
            .shallow()
            .find(ConfigForm)
            .props()
            .onSubmit()
        stubDispatchFunc.simulateSuccess<SaveTribeCommand>()
    } verify {
        stubDispatchFunc.commandsDispatched<SaveTribeCommand>()
            .assertIsEqualTo(listOf(SaveTribeCommand(tribe)))
        wrapper
            .find(Navigate).props().to
            .assertIsEqualTo("/tribes/")
    }

    @Test
    fun whenTribeIsNewWillSuggestIdAutomaticallyAndWillRetainIt() = setup(object {
        val tribe = Tribe(TribeId(""))
        val stubDispatchFunc = StubDispatchFunc<TribeConfigDispatcher>()
        val wrapper = shallow(TribeConfig(tribe, stubDispatchFunc))
        val automatedTribeId = wrapper.find(tribeConfigContent)
            .shallow()
            .find<Any>("#tribe-id")
            .prop("value")
    }) exercise {
        wrapper.find(tribeConfigContent)
            .shallow()
            .find(ConfigForm)
            .props()
            .onSubmit()
    } verify {
        stubDispatchFunc.commandsDispatched<SaveTribeCommand>()
            .first()
            .tribe.id.value.run {
                assertIsNotEqualTo("")
                assertIsEqualTo(automatedTribeId)
            }
        wrapper.find(tribeConfigContent)
            .shallow()
            .find<Any>("#tribe-id")
            .prop("value")
            .assertIsEqualTo(automatedTribeId)
    }

}
