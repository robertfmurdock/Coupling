package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.client.ConfigForm
import com.zegreatrob.coupling.client.StubDispatchFunc
import com.zegreatrob.coupling.client.pairassignments.assertNotNull
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minenzyme.shallow
import com.zegreatrob.minenzyme.simulateInputChange
import com.zegreatrob.testmints.setup
import kotlin.test.Test

class PinConfigEditorTest {

    @Test
    fun whenGivenPinHasNoIdWillNotShowDeleteButton() = setup(object {
        val tribe = Tribe(TribeId(""))
        val pin = Pin(id = null)
    }) exercise {
        shallow(PinConfigEditor(tribe, pin, {}, StubDispatchFunc()))
    } verify { wrapper ->
        wrapper.find(ConfigForm)
            .props()
            .onRemove
            .assertIsEqualTo(null)
    }

    @Test
    fun whenGivenPinHasIdWillShowDeleteButton() = setup(object {
        val tribe = Tribe(TribeId(""))
        val pin = Pin(id = "excellent id")
    }) exercise {
        shallow(PinConfigEditor(tribe, pin, {}, StubDispatchFunc()))
    } verify { wrapper ->
        wrapper.find(ConfigForm)
            .props()
            .onRemove
            .assertNotNull()
    }

    @Test
    fun whenSaveIsPressedWillSavePinWithUpdatedContent() = setup(object {
        val tribe = Tribe(TribeId("dumb tribe"))
        val pin = Pin(id = null, name = "")
        val newName = "pin new name"
        val newIcon = "pin new icon"

        val dispatchFunc = StubDispatchFunc<PinCommandDispatcher>()

        val wrapper = shallow(PinConfigEditor(tribe, pin, {}, dispatchFunc)).apply {
            simulateInputChange("name", newName)
            simulateInputChange("icon", newIcon)
            update()
        }
    }) exercise {
        wrapper.find(ConfigForm)
            .props()
            .onSubmit()
    } verify {
        dispatchFunc.commandsDispatched<SavePinCommand>()
            .assertIsEqualTo(listOf(SavePinCommand(tribe.id, Pin(name = newName, icon = newIcon))))
    }

}
