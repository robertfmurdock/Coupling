package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.client.StubDispatchFunc
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.setup
import findByClass
import shallow2
import simulateInputChange
import kotlin.js.json
import kotlin.test.Test

class PinConfigEditorTest {

    private val styles = useStyles("pin/PinConfigEditor")

    @Test
    fun whenGivenPinHasNoIdWillNotShowDeleteButton() = setup(object {
        val tribe = Tribe(TribeId(""))
        val pin = Pin(_id = null)
    }) exercise {
        shallow2(PinConfigEditor, PinConfigEditorProps(tribe, pin, {}, {}, StubDispatchFunc()))
    } verify { wrapper ->
        wrapper.findByClass(styles["deleteButton"])
            .length
            .assertIsEqualTo(0)
    }

    @Test
    fun whenGivenPinHasIdWillShowDeleteButton() = setup(object {
        val tribe = Tribe(TribeId(""))
        val pin = Pin(_id = "excellent id")
    }) exercise {
        shallow2(PinConfigEditor, PinConfigEditorProps(tribe, pin, {}, {}, StubDispatchFunc()))
    } verify { wrapper ->
        wrapper.findByClass(styles["deleteButton"])
            .length
            .assertIsEqualTo(1)
    }

    @Test
    fun whenSaveIsPressedWillSavePinWithUpdatedContent() = setup(object {
        val tribe = Tribe(TribeId("dumb tribe"))
        val pin = Pin(_id = null, name = "")
        val newName = "pin new name"
        val newIcon = "pin new icon"

        val dispatchFunc = StubDispatchFunc<PinCommandDispatcher>()

        val wrapper = shallow2(PinConfigEditor, PinConfigEditorProps(tribe, pin, {}, {}, dispatchFunc)).apply {
            simulateInputChange("name", newName)
            simulateInputChange("icon", newIcon)
            update()
        }
    }) exercise {
        wrapper.find<Any>("form")
            .simulate("submit", json("preventDefault" to {}))
    } verify {
        dispatchFunc.commandsDispatched<SavePinCommand>()
            .assertIsEqualTo(listOf(SavePinCommand(tribe.id, Pin(name = newName, icon = newIcon))))
    }

}
