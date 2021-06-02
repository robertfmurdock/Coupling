package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.toCouplingServerMessage
import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.model.CouplingSocketMessage
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.stubmodel.stubTribe
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import kotlinx.coroutines.*
import org.w3c.dom.url.URL
import kotlin.js.json
import kotlin.test.Test

@Suppress("UNUSED_PARAMETER")
fun newWebsocket(url: String, options: dynamic): WS = js("new (require('ws'))(url, options)").unsafeCast<WS>()

external interface WS {
    fun on(event: String, callback: (String) -> Unit)
    fun close()
}

class WebsocketTest {

    private fun AuthorizedSdk.baseUrl() = URL(axios.defaults.baseURL.unsafeCast<String>())

    @Test
    fun whenOnlyOneConnectionWillReturnCountOfOne() = asyncSetup(sdkContext {
        object : SdkContext by it {
            val tribe = stubTribe()
        }
    }) {
        sdk.save(tribe)
    } exercise {
        val socket = connectToSocket(sdk, tribe.id)

        val messageDeferred = CompletableDeferred<String>()
        socket.on("message") {
            messageDeferred.complete(it)
            socket.close()
        }
        socket.on("close") {
            messageDeferred.completeExceptionally(Exception("socket closed"))
        }
        messageDeferred.await()
    } verify { result ->
        toCouplingServerMessage(JSON.parse(result))
            .assertIsEqualTo(
                CouplingSocketMessage("Users viewing this page: 1", expectedOnlinePlayerList(username).toSet(), null)
            )
    }

    private fun expectedOnlinePlayerList(username: String) =
        listOf(Player(email = "$username._temp", name = "", id = "-1"))

    @Test
    fun whenMultipleConnectionsWillReturnTheTotalCount() = asyncSetup(sdkContext {
        object : SdkContext by it {
            val tribe = stubTribe()
        }
    }) {
        sdk.save(tribe)
    } exercise {
        val firstTwoSockets = withContext(Dispatchers.Default) {
            val job = launch { }
            listOf(
                openSocket(sdk, tribe, job),
                openSocket(sdk, tribe, job)
            ).map { it.await() }
        }

        val thirdSocket = openSocket(sdk, tribe).await()
        (firstTwoSockets + thirdSocket)
    } verifyAnd { result ->
        result[2].first
            .map { toCouplingServerMessage(JSON.parse(it)) }
            .assertIsEqualTo(
                listOf(
                    CouplingSocketMessage(
                        "Users viewing this page: 3",
                        expectedOnlinePlayerList(username).toSet(),
                        null
                    )
                )
            )
    } teardown { result ->
        result.forEach { it.second.close() }
    }

    @Test
    fun whenNewConnectionIsOpenExistingConnectionsReceiveMessage() = asyncSetup(sdkContext {
        object : SdkContext by it {
            val tribe = stubTribe()
        }
    }) {
        sdk.save(tribe)
    } exercise {
        val socket1 = openSocket(sdk, tribe).await()
        val socket2 = openSocket(sdk, tribe).await()
        listOf(socket1, socket2)
    } verifyAnd { sockets ->
        sockets[0].first.map { toCouplingServerMessage(JSON.parse(it)) }
            .assertIsEqualTo(
                listOf(
                    CouplingSocketMessage("Users viewing this page: 1", expectedOnlinePlayerList(username).toSet()),
                    CouplingSocketMessage("Users viewing this page: 2", expectedOnlinePlayerList(username).toSet())
                )
            )
    } teardown { sockets ->
        sockets.forEach { it.second.close() }
    }

    @Test
    fun whenConnectionClosesOtherConnectionsGetMessageWithNewCount() = asyncSetup(sdkContext {
        object : SdkContext by it {
            val tribe = stubTribe()
        }
    }) {
        sdk.save(tribe)
    } exercise {
        val socketToClose = openSocket(sdk, tribe).await()
        openSocket(sdk, tribe).await()
            .also {
                val deferred = CompletableDeferred<Unit>()
                it.second.on("message") {
                    deferred.complete(Unit)
                }
                socketToClose.second.close()
                deferred.await()
            }
    } verifyAnd { openSocket ->
        openSocket.first.map { toCouplingServerMessage(JSON.parse(it)) }
            .assertIsEqualTo(
                listOf(
                    CouplingSocketMessage("Users viewing this page: 2", expectedOnlinePlayerList(username).toSet()),
                    CouplingSocketMessage("Users viewing this page: 1", expectedOnlinePlayerList(username).toSet())
                )
            )
    } teardown { openSocket ->
        openSocket.second.close()
    }


    @Test
    fun whenNotAuthenticatedDoesNotTalkToYou() = asyncSetup(sdkContext { it }
    ) exercise {
        val baseUrl = sdk.baseUrl()
        val host = baseUrl.host
        val url = "ws://$host/api/${TribeId("whoops").value}/pairAssignments/current"
        val socket = newWebsocket(url, json())
        CompletableDeferred<Unit>().also { deferred ->
            socket.on("close") { deferred.complete(Unit) }
        }
    } verify { deferred ->
        withTimeout(100) {
            deferred.await()
        }
    }

    @Test
    fun whenNotAuthorizedForTheTribeWillNotTalkToYou() = asyncSetup(sdkContext { it }
    ) exercise {
        val socket = connectToSocket(sdk, stubTribe().id)
        CompletableDeferred<Unit>().also { deferred ->
            socket.on("close") { deferred.complete(Unit) }
        }
    } verify { deferred ->
        withTimeout(200) {
            deferred.await()
        }
    }

    @Test
    fun willNotCrashWhenGoingToNonExistingSocketLocation() = asyncSetup(sdkContext { it }
    ) exercise {
        val baseUrl = sdk.baseUrl()
        val host = baseUrl.host
        val url = "ws://$host/api/404WTF"
        val socket = newWebsocket(url, json())
        CompletableDeferred<Unit>().also { deferred ->
            socket.on("close") { deferred.complete(Unit) }
        }
    } verify { deferred ->
        withTimeout(100) {
            deferred.await()
        }
    }

    @Test
    fun whenSocketIsImmediatelyClosedDoesNotCrashServer() = asyncSetup(sdkContext {
        object : SdkContext by it {
            val tribe = stubTribe()
        }
    }) {
        sdk.save(tribe)
    } exercise {
        val socket = connectToSocket(sdk, tribe.id)
        val messageDeferred = CompletableDeferred<Unit>()
        socket.on("open") {
            socket.close()
        }
        socket.on("close") {
            messageDeferred.complete(Unit)
        }
        messageDeferred
    } verify { deferred ->
        withTimeout(100) {
            deferred.await()
        }
    }

    private fun openSocket(
        sdk: Sdk,
        tribe: Tribe,
        parent: Job? = null
    ): CompletableDeferred<Pair<MutableList<String>, WS>> {
        val socket = connectToSocket(sdk, tribe.id)
        val messageDeferred = CompletableDeferred<Pair<MutableList<String>, WS>>(parent)
        val messages = mutableListOf<String>()
        socket.on("message") {
            messages.add(it)
            messageDeferred.complete(Pair(messages, socket))
        }
        socket.on("close") {
            messageDeferred.completeExceptionally(Exception("socket closed"))
        }
        return messageDeferred
    }

    private fun expectedConnectionMessage(count: Int, players: List<Player>) = json(
        "type" to "LivePlayers",
        "text" to "Users viewing this page: $count",
        "players" to players.map { it.toJson() },
        "currentPairAssignments" to null
    ).let { JSON.stringify(it) }

    private fun connectToSocket(sdk: Sdk, tribeId: TribeId): WS {
        val baseUrl = URL(sdk.axios.defaults.baseURL.unsafeCast<String>())
        val host = baseUrl.host
        val url = "ws://$host/api/${tribeId.value}/pairAssignments/current"
        val cookieStringSync =
            sdk.axios.defaults.jar.getCookieStringSync(baseUrl.href).unsafeCast<String>()
        return newWebsocket(url, json("headers" to json("cookie" to cookieStringSync)))
    }

}