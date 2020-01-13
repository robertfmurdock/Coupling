package com.zegreatrob.coupling.client.pin

import Spy
import SpyData
import com.zegreatrob.coupling.client.external.react.PropsClassProvider
import com.zegreatrob.coupling.client.external.react.loadStyles
import com.zegreatrob.coupling.client.external.react.provider
import com.zegreatrob.coupling.client.player.PlayerConfigStyles
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

class PinConfigTest {

    private val styles = loadStyles<PlayerConfigStyles>("pin/PinConfig")

    abstract class RendererWithStub : PinConfigRenderer, PropsClassProvider<PinConfigProps> by provider() {
        override val pinRepository: PinRepository get() = throw NotImplementedError("stubbed")
    }

    @Test
    fun whenGivenPinHasNoIdWillNotShowDeleteButton() = setup(object : RendererWithStub() {
        val tribe = Tribe(TribeId(""))
        val pin = Pin(_id = null)

    }) exercise {
        shallow(PinConfigProps(tribe, pin, {}, {}))
    } verify { wrapper ->
        wrapper.findByClass(styles.deleteButton)
            .length
            .assertIsEqualTo(0)
    }

    @Test
    fun whenGivenPinHasIdWillShowDeleteButton() = setup(object : RendererWithStub() {
        val tribe = Tribe(TribeId(""))
        val pin = Pin(_id = "excellent id")

    }) exercise {
        shallow(PinConfigProps(tribe, pin, {}, {}))
    } verify { wrapper ->
        wrapper.findByClass(styles.deleteButton)
            .length
            .assertIsEqualTo(1)
    }

    @Test
    fun whenSaveIsPressedWillSavePinWithUpdatedContent() = testAsync {
        withContext(coroutineContext) {
            setupAsync(object : RendererWithStub() {
                override fun buildScope() = this@withContext
                val tribe = Tribe(TribeId("dumb tribe"))
                val pin = Pin(_id = null, name = null)
                val wrapper = shallow(PinConfigProps(tribe, pin, {}, {}))
                val newName = "pin new name"

                val savePinSpy = object : Spy<SavePinCommand, Unit> by SpyData() {}.apply { spyWillReturn(Unit) }

                override fun SavePinCommand.perform() = savePinSpy.spyFunction(this)

                init {
                    wrapper.simulateInputChange("name", newName)
                    wrapper.update()
                }
            }) exerciseAsync {
                wrapper.find<Any>("form")
                    .simulate("submit", json("preventDefault" to {}))
            }
        } verifyAsync {
            savePinSpy.spyReceivedValues
                .assertIsEqualTo(
                    listOf(
                        SavePinCommand(tribe.id, Pin(name = newName))
                    )
                )
        }
    }
}