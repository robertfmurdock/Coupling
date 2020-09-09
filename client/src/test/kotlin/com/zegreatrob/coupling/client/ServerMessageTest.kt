package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.external.reactwebsocket.WebsocketProps
import com.zegreatrob.coupling.client.user.CouplingSocketMessage
import com.zegreatrob.coupling.client.user.ServerMessage
import com.zegreatrob.coupling.client.user.ServerMessageProps
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minenzyme.shallow
import com.zegreatrob.testmints.invoke
import com.zegreatrob.testmints.setup
import react.RClass
import kotlin.test.Test

@JsModule("react-websocket")
external val websocket: RClass<WebsocketProps>

class ServerMessageTest {

    @Test
    fun displaysServerMessage(): Unit = setup(object {
        val expectedMessage = "Hi it me"
        val props = ServerMessageProps(TribeId("bwahahahaha"), CouplingSocketMessage(expectedMessage, listOf(), null))
        val wrapper = shallow(ServerMessage, props)
    }) exercise {
        wrapper.update()
    } verify {
        wrapper.find<Any>("span").text()
            .assertIsEqualTo(expectedMessage)
    }

}
