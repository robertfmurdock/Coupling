package com.zegreatrob.coupling.sdk

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.setupAsync
import com.zegreatrob.testmints.async.testAsync
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

    @Test
    fun whenOnlyOneConnectionWillReturnCountOfOne() = testAsync {
        val username = "eT-user-${uuid4()}"
        val sdk = authorizedSdk(username = username)
        setupAsync(object {
            val tribe = Tribe(TribeId(uuid4().toString()))
        }) {
            sdk.save(tribe)
        } exerciseAsync {
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
        } verifyAsync { result ->
            result.assertIsEqualTo(
                expectedConnectionMessage(1, expectedUserList(username))
            )
        }
    }

    private fun expectedUserList(username: String) = listOf(Player(email = "$username._temp", name = "", id = "-1"))

    @Test
    fun whenMultipleConnectionsWillReturnTheTotalCount() = testAsync {
        val username = "eT-user-${uuid4()}"
        val sdk = authorizedSdk(username = username)
        setupAsync(object {
            val tribe = Tribe(TribeId(uuid4().toString()))
        }) {
            sdk.save(tribe)
        } exerciseAsync {
            val firstTwoSockets = withContext(Dispatchers.Default) {
                val job = launch { }
                listOf(
                    openSocket(sdk, tribe, job),
                    openSocket(sdk, tribe, job)
                ).map { it.await() }
            }

            val thirdSocket = openSocket(sdk, tribe).await()
            (firstTwoSockets + thirdSocket)
        } verifyAsync { result ->
            result[2].first.assertIsEqualTo(
                mutableListOf(expectedConnectionMessage(3, expectedUserList(username)))
            )
            result.forEach { it.second.close() }
        }
    }

    @Test
    fun whenNewConnectionIsOpenExistingConnectionsReceiveMessageWithNewCount() = testAsync {
        val username = "eT-user-${uuid4()}"
        val sdk = authorizedSdk(username = username)
        setupAsync(object {
            val tribe = Tribe(TribeId(uuid4().toString()))
        }) {
            sdk.save(tribe)
        } exerciseAsync {
            val socket1 = openSocket(sdk, tribe).await()
            val socket2 = openSocket(sdk, tribe).await()
            listOf(socket1, socket2)
        } verifyAsync { sockets ->
            sockets[0].first.assertIsEqualTo(
                mutableListOf(
                    expectedConnectionMessage(1, expectedUserList(username)),
                    expectedConnectionMessage(2, expectedUserList(username))
                )
            )
            sockets.forEach { it.second.close() }
        }
    }

    @Test
    fun whenConnectionClosesOtherConnectionsGetMessageWithNewCount() = testAsync {
        val username = "eT-user-${uuid4()}"
        val sdk = authorizedSdk(username = username)
        setupAsync(object {
            val tribe = Tribe(TribeId(uuid4().toString()))
        }) {
            sdk.save(tribe)
        } exerciseAsync {
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
        } verifyAsync { openSocket ->
            openSocket.first.assertIsEqualTo(
                mutableListOf(
                    expectedConnectionMessage(2, expectedUserList(username)),
                    expectedConnectionMessage(1, expectedUserList(username))
                )
            )
            openSocket.second.close()
        }
    }

    @Test
    fun whenNotAuthenticatedDoesNotTalkToYou() = testAsync {
        val sdk = authorizedSdk()
        setupAsync(object {
        }) exerciseAsync {
            val baseUrl = URL(sdk.axios.defaults.baseURL.unsafeCast<String>())
            val host = baseUrl.host
            val url = "ws://$host/api/${TribeId("whoops").value}/pairAssignments/current"
            val socket = newWebsocket(url, json())
            CompletableDeferred<Unit>().also { deferred ->
                socket.on("close") { deferred.complete(Unit) }
            }
        } verifyAsync { deferred ->
            withTimeout(100) {
                deferred.await()
            }
        }
    }

    @Test
    fun whenNotAuthorizedForTheTribeWillNotTalkToYou() = testAsync {
        val sdk = authorizedSdk()
        setupAsync(object {
        }) exerciseAsync {
            val socket = connectToSocket(sdk, TribeId("thisIsNonsense"))
            CompletableDeferred<Unit>().also { deferred ->
                socket.on("close") { deferred.complete(Unit) }
            }
        } verifyAsync { deferred ->
            withTimeout(100) {
                deferred.await()
            }
        }
    }

    @Test
    fun willNotCrashWhenGoingToNonExistingSocketLocation() = testAsync {
        val sdk = authorizedSdk()
        setupAsync(object {
        }) exerciseAsync {
            val baseUrl = URL(sdk.axios.defaults.baseURL.unsafeCast<String>())
            val host = baseUrl.host
            val url = "ws://$host/api/404WTF"
            val socket = newWebsocket(url, json())
            CompletableDeferred<Unit>().also { deferred ->
                socket.on("close") { deferred.complete(Unit) }
            }
        } verifyAsync { deferred ->
            withTimeout(100) {
                deferred.await()
            }
        }
    }

    @Test
    fun whenSocketIsImmediatelyClosedDoesNotCrashServer() = testAsync {
        val username = "eT-user-${uuid4()}"
        val sdk = authorizedSdk(username = username)
        setupAsync(object {
            val tribe = Tribe(TribeId(uuid4().toString()))
        }) {
            sdk.save(tribe)
        } exerciseAsync {
            val socket = connectToSocket(sdk, tribe.id)
            val messageDeferred = CompletableDeferred<Unit>()
            socket.on("open") {
                socket.close()
            }
            socket.on("close") {
                messageDeferred.complete(Unit)
            }
            messageDeferred
        } verifyAsync { deferred ->
            withTimeout(100) {
                deferred.await()
            }
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
        "players" to players.map { it.toJson() }
    ).let { JSON.stringify(it) }

    private fun connectToSocket(sdk: Sdk, tribeId: TribeId): WS {
        val baseUrl = URL(sdk.axios.defaults.baseURL.unsafeCast<String>())
        val host = baseUrl.host
        val url = "ws://$host/api/${tribeId.value}/pairAssignments/current"
        val cookieStringSync =
            sdk.axios.defaults.jar.getCookieStringSync(baseUrl.href).unsafeCast<String>()
        return newWebsocket(
            url,
            json("headers" to json("cookie" to cookieStringSync))
        )
    }

}