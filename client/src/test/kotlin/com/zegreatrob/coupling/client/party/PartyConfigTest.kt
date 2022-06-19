package com.zegreatrob.coupling.client.party

import com.zegreatrob.coupling.client.ConfigForm
import com.zegreatrob.coupling.client.StubDispatchFunc
import com.zegreatrob.coupling.client.StubDispatcher
import com.zegreatrob.coupling.model.party.PairingRule
import com.zegreatrob.coupling.model.party.PairingRule.Companion.toValue
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minassert.assertIsNotEqualTo
import com.zegreatrob.minenzyme.ShallowWrapper
import com.zegreatrob.minenzyme.shallow
import com.zegreatrob.testmints.setup
import react.router.Navigate
import kotlin.test.Test

class PartyConfigTest {

    @Test
    fun willDefaultPartyThatIsMissingData(): Unit = setup(object {
        val party = Party(PartyId("1"), name = "1")
    }) exercise {
        shallow(PartyConfig(party, StubDispatchFunc()))
            .find(partyConfigContent)
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
    fun whenClickTheSaveButtonWillUseCouplingServiceToSaveTheParty() = setup(object {
        val party = Party(
            PartyId("1"),
            name = "1",
            alternateBadgeName = "alt",
            defaultBadgeName = "def",
            email = "email-y",
            pairingRule = PairingRule.PreferDifferentBadge
        )
        val stubDispatcher = StubDispatcher()
        val wrapper = shallow(PartyConfig(party, stubDispatcher.func()))
    }) exercise {
        wrapper.find(partyConfigContent)
            .shallow()
            .find(ConfigForm)
            .props()
            .onSubmit()
        stubDispatcher.simulateSuccess<SavePartyCommand>()
    } verify {
        stubDispatcher.commandsDispatched<SavePartyCommand>()
            .assertIsEqualTo(listOf(SavePartyCommand(party)))
        wrapper
            .find(Navigate).props().to
            .assertIsEqualTo("/tribes/")
    }

    @Test
    fun whenPartyIsNewWillSuggestIdAutomaticallyAndWillRetainIt() = setup(object {
        val party = Party(PartyId(""))
        val stubDispatcher = StubDispatcher()
        val wrapper = shallow(PartyConfig(party, stubDispatcher.func()))
        val automatedPartyId = wrapper.find(partyConfigContent)
            .shallow()
            .find<Any>("#tribe-id")
            .prop("value")
    }) exercise {
        wrapper.find(partyConfigContent)
            .shallow()
            .find(ConfigForm)
            .props()
            .onSubmit()
    } verify {
        stubDispatcher.commandsDispatched<SavePartyCommand>()
            .first()
            .party.id.value.run {
                assertIsNotEqualTo("")
                assertIsEqualTo(automatedPartyId)
            }
        wrapper.find(partyConfigContent)
            .shallow()
            .find<Any>("#tribe-id")
            .prop("value")
            .assertIsEqualTo(automatedPartyId)
    }
}
