package com.zegreatrob.coupling.client.components

import com.zegreatrob.coupling.model.CouplingSocketMessage
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.setup
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.render
import org.w3c.dom.HTMLElement
import kotlin.test.Test

class ServerMessageTest {

    @Test
    fun displaysServerMessage(): Unit = setup(object {
        val expectedMessage = "Hi it me"
    }) exercise {
        render(ServerMessage.create(CouplingSocketMessage(expectedMessage, emptySet(), null)))
    } verify { wrapper ->
        wrapper.baseElement.getElementsByTagName("span").item(0)
            .let { it as? HTMLElement }
            ?.textContent
            .assertIsEqualTo(expectedMessage)
    }
}
