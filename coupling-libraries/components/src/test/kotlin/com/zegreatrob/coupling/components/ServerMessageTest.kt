package com.zegreatrob.coupling.components

import com.zegreatrob.coupling.model.CouplingSocketMessage
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minenzyme.shallow
import com.zegreatrob.testmints.setup
import kotlin.test.Test

class ServerMessageTest {

    @Test
    fun displaysServerMessage(): Unit = setup(object {
        val expectedMessage = "Hi it me"
        val wrapper = shallow(
            ServerMessage(CouplingSocketMessage(expectedMessage, emptySet(), null))
        )
    }) exercise {
        wrapper.update()
    } verify {
        wrapper.find<Any>("span").text()
            .assertIsEqualTo(expectedMessage)
    }
}
