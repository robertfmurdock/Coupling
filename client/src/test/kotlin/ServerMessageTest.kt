import com.zegreatrob.coupling.client.external.react.PropsClassProvider
import com.zegreatrob.coupling.client.external.react.provider
import com.zegreatrob.coupling.client.user.ServerMessageProps
import com.zegreatrob.coupling.client.user.ServerMessageRenderer
import com.zegreatrob.coupling.client.user.WebsocketProps
import com.zegreatrob.coupling.core.entity.tribe.TribeId
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.setup
import react.RClass
import kotlin.browser.window
import kotlin.js.Json
import kotlin.js.json
import kotlin.test.Test


@JsModule("react-websocket")

external val websocket: RClass<WebsocketProps>

class ServerMessageTest : ServerMessageRenderer, PropsClassProvider<ServerMessageProps> by provider() {

    @Test
    fun connectsToTheWebsocketUsingTribe(): Unit = setup(
        ServerMessageProps(tribeId = TribeId("bwahahahaha"), useSsl = false)
    ) exercise {
        shallow(this@exercise)
    } verify { wrapper ->
        wrapper.find(websocket).props()
            .url
            .assertIsEqualTo(
                "ws://${window.location.host}/api/${tribeId.value}/pairAssignments/current"
            )
    }

    @Test
    fun whenSslIsOnWillUseHttps() = setup(
        ServerMessageProps(tribeId = TribeId("LOL"), useSsl = true)
    ) exercise {
        shallow(this@exercise)
    } verify { wrapper ->
        wrapper.find(websocket).props()
            .url
            .assertIsEqualTo(
                "wss://${window.location.host}/api/LOL/pairAssignments/current"
            )
    }

    @Test
    fun displaysServerMessage(): Unit = setup(object {
        val props = ServerMessageProps(tribeId = TribeId("bwahahahaha"), useSsl = false)
        val wrapper = shallow(props)
        val websocketProps = wrapper.find(websocket).props()
        val expectedMessage = "Hi it me"
    }) exercise {
        websocketProps.onMessage(socketMessage(expectedMessage))
        wrapper.update()
    } verify {
        wrapper.find<Any>("span").text()
            .assertIsEqualTo(
                expectedMessage
            )
    }

    private fun socketMessage(expectedMessage: String) =
        JSON.stringify(json("type" to "LivePlayers", "text" to expectedMessage, "players" to emptyArray<Json>()))

    @Test
    fun displaysNotConnectedMessageWhenSocketIsClosed(): Unit = setup(object {
        val props = ServerMessageProps(tribeId = TribeId("bwahahahaha"), useSsl = false)
        val wrapper = shallow(props)
        val websocketProps = wrapper.find(websocket).props()
            .unsafeCast<WebsocketProps>()
        val expectedMessage = "Not connected"
    }) exercise {
        websocketProps.onMessage(socketMessage("lol"))
        wrapper.update()
        websocketProps.onClose()
        wrapper.update()
    } verify {
        wrapper.find<Any>("span").text()
            .assertIsEqualTo(
                expectedMessage
            )
    }

}