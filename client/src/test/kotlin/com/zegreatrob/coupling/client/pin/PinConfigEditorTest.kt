package com.zegreatrob.coupling.client.pin

import SpyData
import com.zegreatrob.coupling.client.exerciseScopeProvider
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.pairassignments.NullTraceIdProvider
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.pin.PinRepository
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.ScopeMint
import com.zegreatrob.testmints.async.setupAsync2
import com.zegreatrob.testmints.setup
import findByClass
import shallow
import simulateInputChange
import kotlin.js.json
import kotlin.test.Test

class PinConfigEditorTest {

    private val styles = useStyles("pin/PinConfigEditor")

    private val stubDispatcher = object : PinCommandDispatcher, NullTraceIdProvider {
        override val pinRepository: PinRepository get() = throw NotImplementedError("stubbed")
    }

    @Test
    fun whenGivenPinHasNoIdWillNotShowDeleteButton() = setup(object {
        val tribe = Tribe(TribeId(""))
        val pin = Pin(_id = null)
    }) exercise {
        shallow(PinConfigEditor, PinConfigEditorProps(tribe, pin, {}, {}, stubDispatcher))
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
        shallow(PinConfigEditor, PinConfigEditorProps(tribe, pin, {}, {}, stubDispatcher))
    } verify { wrapper ->
        wrapper.findByClass(styles["deleteButton"])
            .length
            .assertIsEqualTo(1)
    }

    @Test
    fun whenSaveIsPressedWillSavePinWithUpdatedContent() = setupAsync2(object : ScopeMint() {
        val stubDispatcher = object : PinCommandDispatcher, NullTraceIdProvider {
            val savePinSpy = SpyData<SavePinCommand, Unit>().apply { spyWillReturn(Unit) }
            override val pinRepository: PinRepository get() = throw NotImplementedError("stubbed")
            override suspend fun SavePinCommand.perform() = savePinSpy.spyFunction(this)
        }
        val tribe = Tribe(TribeId("dumb tribe"))
        val pin = Pin(_id = null, name = "")
        val newName = "pin new name"
        val newIcon = "pin new icon"

        val wrapper = shallow(
            PinConfigEditor(exerciseScopeProvider()), PinConfigEditorProps(tribe, pin, {}, {}, stubDispatcher)
        ).apply {
            simulateInputChange("name", newName)
            simulateInputChange("icon", newIcon)
            update()
        }
    }) exercise {
        wrapper.find<Any>("form")
            .simulate("submit", json("preventDefault" to {}))
    } verify {
        stubDispatcher.savePinSpy.spyReceivedValues
            .assertIsEqualTo(listOf(SavePinCommand(tribe.id, Pin(name = newName, icon = newIcon))))
    }

}
