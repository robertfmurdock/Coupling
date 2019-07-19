import com.zegreatrob.coupling.client.ServerMessageProps
import com.zegreatrob.coupling.client.ServerMessageRenderer.Companion.serverMessage
import com.zegreatrob.coupling.client.WebsocketProps
import com.zegreatrob.coupling.common.entity.tribe.TribeId
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.setup
import react.RClass
import kotlin.browser.window
import kotlin.test.Test


@JsModule("react-websocket")
@JsNonModule
external val websocket: RClass<WebsocketProps>

class ServerMessageTest {

    @Test
    fun connectsToTheWebsocketUsingTribe(): Unit = setup(
            ServerMessageProps(tribeId = TribeId("bwahahahaha"), useSsl = false)
    ) exercise {
        serverMessage.shallowRender(props = this)
    } verify { wrapper ->
        wrapper.find(websocket).props()
                .url
                .assertIsEqualTo(
                        "ws://${window.location.host}/api/${tribeId.value}/pairAssignments/current"
                )
    }

    @Test
    fun whenSslIsOnWillUseHttps() = setup(ServerMessageProps(tribeId = TribeId("LOL"), useSsl = true)
    ) exercise {
        serverMessage.shallowRender(props = this)
    } verify { wrapper ->
        wrapper.find(websocket).props()
                .url
                .assertIsEqualTo(
                        "wss://${window.location.host}/api/LOL/pairAssignments/current"
                )
    }

    @Test
    fun displaysServerMessage(): Unit = setup(object {
        val wrapper = serverMessage.shallowRender(ServerMessageProps(tribeId = TribeId("bwahahahaha"), useSsl = false))
        val websocketProps = wrapper.find(websocket).props()
        val expectedMessage = "Hi it me"
    }) exercise {
        websocketProps.onMessage(expectedMessage)
        wrapper.update()
    } verify {
        wrapper.find("span").text()
                .assertIsEqualTo(
                        expectedMessage
                )
    }

    @Test
    fun displaysNotConnectedMessageWhenSocketIsClosed(): Unit = setup(object {
        val wrapper = serverMessage.shallowRender(ServerMessageProps(tribeId = TribeId("bwahahahaha"), useSsl = false))
        val websocketProps = wrapper.find(websocket).props()
                .unsafeCast<WebsocketProps>()
        val expectedMessage = "Not connected"
    }) exercise {
        websocketProps.onMessage("lol")
        wrapper.update()
        websocketProps.onClose()
        wrapper.update()
    } verify {
        wrapper.find("span").text()
                .assertIsEqualTo(
                        expectedMessage
                )
    }

}