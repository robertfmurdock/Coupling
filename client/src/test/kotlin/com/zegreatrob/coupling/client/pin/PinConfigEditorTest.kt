package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.client.StubDispatchFunc
import com.zegreatrob.coupling.enzyme.external.findByClass
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.enzyme.external.shallow
import com.zegreatrob.coupling.enzyme.external.simulateInputChange
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.invoke
import com.zegreatrob.testmints.setup
import kotlin.js.json
import kotlin.test.Test

class PinConfigEditorTest {

    private val configFormStyles = useStyles("ConfigForm")

    @Test
    fun whenGivenPinHasNoIdWillNotShowDeleteButton() = setup(object {
        val tribe = Tribe(TribeId(""))
        val pin = Pin(_id = null)
    }) exercise {
        shallow(
            PinConfigEditor,
            PinConfigEditorProps(tribe, pin, {}, {}, StubDispatchFunc())
        )
    } verify { wrapper ->
        wrapper.findByClass(configFormStyles["deleteButton"])
            .length
            .assertIsEqualTo(0)
    }

    @Test
    fun whenGivenPinHasIdWillShowDeleteButton() = setup(object {
        val tribe = Tribe(TribeId(""))
        val pin = Pin(_id = "excellent id")
    }) exercise {
        shallow(
            PinConfigEditor,
            PinConfigEditorProps(tribe, pin, {}, {}, StubDispatchFunc())
        )
    } verify { wrapper ->
        wrapper.findByClass(configFormStyles["deleteButton"])
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

        val wrapper = shallow(
            PinConfigEditor,
            PinConfigEditorProps(tribe, pin, {}, {}, dispatchFunc)
        ).apply {
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
