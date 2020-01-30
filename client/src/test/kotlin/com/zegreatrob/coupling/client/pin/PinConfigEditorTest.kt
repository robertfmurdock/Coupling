package com.zegreatrob.coupling.client.pin

import Spy
import SpyData
import com.zegreatrob.coupling.client.external.react.FRComponent
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.provider
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.pin.PinRepository
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.setupAsync
import com.zegreatrob.testmints.async.testAsync
import com.zegreatrob.testmints.setup
import findByClass
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.withContext
import shallow
import simulateInputChange
import kotlin.js.json
import kotlin.test.Test

class PinConfigEditorTest {

    private val styles = useStyles("pin/PinConfigEditor")

    abstract class RendererWithStub : FRComponent<PinConfigEditorProps>(provider()), PinConfigEditorRenderer {
        override val pinRepository: PinRepository get() = throw NotImplementedError("stubbed")
    }

    @Test
    fun whenGivenPinHasNoIdWillNotShowDeleteButton() = setup(object : RendererWithStub() {
        val tribe = Tribe(TribeId(""))
        val pin = Pin(_id = null)

    }) exercise {
        shallow(PinConfigEditorProps(tribe, pin, {}, {}, MainScope()))
    } verify { wrapper ->
        wrapper.findByClass(styles["deleteButton"])
            .length
            .assertIsEqualTo(0)
    }

    @Test
    fun whenGivenPinHasIdWillShowDeleteButton() = setup(object : RendererWithStub() {
        val tribe = Tribe(TribeId(""))
        val pin = Pin(_id = "excellent id")
    }) exercise {
        shallow(PinConfigEditorProps(tribe, pin, {}, {}, MainScope()))
    } verify { wrapper ->
        wrapper.findByClass(styles["deleteButton"])
            .length
            .assertIsEqualTo(1)
    }

    @Test
    fun whenSaveIsPressedWillSavePinWithUpdatedContent() = testAsync {
        withContext(coroutineContext) {
            setupAsync(object : RendererWithStub() {
                val tribe = Tribe(TribeId("dumb tribe"))
                val pin = Pin(_id = null, name = null)
                val wrapper = shallow(PinConfigEditorProps(tribe, pin, {}, {}, this@withContext))
                val newName = "pin new name"
                val newIcon = "pin new icon"

                val savePinSpy = object : Spy<SavePinCommand, Unit> by SpyData() {}.apply { spyWillReturn(Unit) }

                override suspend fun SavePinCommand.perform() = savePinSpy.spyFunction(this)

            }) {
                wrapper.simulateInputChange("name", newName)
                wrapper.simulateInputChange("icon", newIcon)
                wrapper.update()
            } exerciseAsync {
                wrapper.find<Any>("form")
                    .simulate("submit", json("preventDefault" to {}))
            }
        } verifyAsync {
            savePinSpy.spyReceivedValues
                .assertIsEqualTo(
                    listOf(
                        SavePinCommand(tribe.id, Pin(name = newName, icon = newIcon))
                    )
                )
        }
    }
}