package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.client.ConfigForm
import com.zegreatrob.coupling.client.StubDispatchFunc
import com.zegreatrob.coupling.client.StubDispatcher
import com.zegreatrob.coupling.client.pairassignments.assertNotNull
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minenzyme.shallow
import com.zegreatrob.minenzyme.simulateInputChange
import com.zegreatrob.testmints.setup
import kotlin.test.Test

class PinConfigEditorTest {

    @Test
    fun whenGivenPinHasNoIdWillNotShowDeleteButton() = setup(object {
        val party = Party(PartyId(""))
        val pin = Pin(id = null)
    }) exercise {
        shallow(PinConfig(party, pin, emptyList(), {}, StubDispatchFunc()))
            .find(pinConfigContent)
            .shallow()
    } verify { wrapper ->
        wrapper.find(ConfigForm)
            .props()
            .onRemove
            .assertIsEqualTo(null)
    }

    @Test
    fun whenGivenPinHasIdWillShowDeleteButton() = setup(object {
        val party = Party(PartyId(""))
        val pin = Pin(id = "excellent id")
    }) exercise {
        shallow(PinConfig(party, pin, emptyList(), {}, StubDispatchFunc()))
            .find(pinConfigContent)
            .shallow()
    } verify { wrapper ->
        wrapper.find(ConfigForm)
            .props()
            .onRemove
            .assertNotNull()
    }

    @Test
    fun whenSaveIsPressedWillSavePinWithUpdatedContent() = setup(object {
        val party = Party(PartyId("dumb tribe"))
        val pin = Pin(id = null, name = "")
        val newName = "pin new name"
        val newIcon = "pin new icon"

        val stubDispatcher = StubDispatcher()

        val wrapper = shallow(PinConfig(party, pin, emptyList(), {}, stubDispatcher.func()))
    }) {
        wrapper.find(pinConfigContent).shallow().apply {
            simulateInputChange("name", newName)
            simulateInputChange("icon", newIcon)
            update()
        }
    } exercise {
        wrapper.find(pinConfigContent)
            .shallow()
            .find(ConfigForm)
            .props()
            .onSubmit()
    } verify {
        stubDispatcher.commandsDispatched<SavePinCommand>()
            .assertIsEqualTo(listOf(SavePinCommand(party.id, Pin(name = newName, icon = newIcon))))
    }
}
