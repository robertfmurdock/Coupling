package com.zegreatrob.coupling.client.pin

import Spy
import SpyData
import com.zegreatrob.coupling.client.external.react.ReactScopeProvider
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.pairassignments.NullTraceIdProvider
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.pin.PinRepository
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.setupAsync
import com.zegreatrob.testmints.async.testAsync
import com.zegreatrob.testmints.setup
import findByClass
import kotlinx.coroutines.withContext
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
    fun whenSaveIsPressedWillSavePinWithUpdatedContent() = testAsync {
        withContext(coroutineContext) {
            setupAsync(object : ReactScopeProvider {
                val tribe = Tribe(TribeId("dumb tribe"))
                val pin = Pin(_id = null, name = "")
                val newName = "pin new name"
                val newIcon = "pin new icon"
                val stubDispatcher = object : PinCommandDispatcher, NullTraceIdProvider {
                    override val pinRepository: PinRepository get() = throw NotImplementedError("stubbed")
                    override suspend fun SavePinCommand.perform() = savePinSpy.spyFunction(this)
                }

                override fun buildScope() = this@withContext

                val savePinSpy = object : Spy<SavePinCommand, Unit> by SpyData() {}.apply { spyWillReturn(Unit) }
                val wrapper = shallow(PinConfigEditor(this), PinConfigEditorProps(tribe, pin, {}, {}, stubDispatcher))
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