package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.user.ServerMessage
import com.zegreatrob.coupling.model.CouplingSocketMessage
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minenzyme.shallow
import com.zegreatrob.testmints.setup
import kotlin.test.Test

class ServerMessageTest {

    @Test
    fun displaysServerMessage(): Unit = setup(object {
        val expectedMessage = "Hi it me"
        val wrapper = shallow(
            ServerMessage(TribeId("bwahahahaha"), CouplingSocketMessage(expectedMessage, emptySet(), null))
        )
    }) exercise {
        wrapper.update()
    } verify {
        wrapper.find<Any>("span").text()
            .assertIsEqualTo(expectedMessage)
    }

}
