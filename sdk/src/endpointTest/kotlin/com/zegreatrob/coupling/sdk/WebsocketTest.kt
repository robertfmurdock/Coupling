package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.JsonCouplingSocketMessage
import com.zegreatrob.coupling.json.JsonMessage
import com.zegreatrob.coupling.json.fromJsonString
import com.zegreatrob.coupling.json.toModel
import com.zegreatrob.coupling.model.CouplingSocketMessage
import com.zegreatrob.coupling.model.Message
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.stubmodel.stubTribe
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import io.ktor.client.features.cookies.*
import io.ktor.http.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
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
    fun whenOnlyOneConnectionWillReturnCountOfOne() = asyncSetup(sdkContext {
        object : SdkContext by it {
            val tribe = stubTribe()
        }
    }) {
        sdk.save(tribe)
    } exercise {
        val socket = connectToSocket(tribe.id, getCookieString(sdk))
        val messageDeferred = CompletableDeferred<String>()
        socket.on("message") { if(!messageDeferred.isCompleted) messageDeferred.complete(it) }
        socket.on("close") {
            if(!messageDeferred.isCompleted) messageDeferred.completeExceptionally(Exception("socket closed"))
        }
        socket to messageDeferred.await()
    } verifyAnd { (_, message) ->
        message.toCouplingServerMessage()
            .assertIsEqualTo(
                CouplingSocketMessage("Users viewing this page: 1", expectedOnlinePlayerList(username).toSet(), null)
            )
    } teardown { (socket) ->
        socket.close()
    }

    @Test
    fun whenMultipleConnectionsWillReturnTheTotalCount() = asyncSetup(sdkContext {
        object : SdkContext by it {
            val tribe = stubTribe()
        }
    }) {
        sdk.save(tribe)
    } exercise {
        val cookieString = getCookieString(sdk)
        val firstTwoSockets = listOf(
            openSocket(tribe, cookieString),
            openSocket(tribe, cookieString)
        ).map { it.await() }

        val thirdSocket = openSocket(tribe, cookieString).await()
        (firstTwoSockets + thirdSocket)
    } verifyAnd { result ->
        result[2].first
            .map(String::toCouplingServerMessage)
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
//
//    @Test
//    fun whenNewConnectionIsOpenExistingConnectionsReceiveMessage() = asyncSetup(sdkContext {
//        object : SdkContext by it {
//            val tribe = stubTribe()
//        }
//    }) {
//        sdk.save(tribe)
//    } exercise {
//        val cookieString = getCookieString(sdk)
//        val socket1 = openSocket(tribe, cookieString).await()
//        val socket2 = openSocket(tribe, cookieString).await()
//        listOf(socket1, socket2)
//    } verifyAnd { sockets ->
//        sockets[0].first.map(String::toCouplingServerMessage)
//            .assertIsEqualTo(
//                listOf(
//                    CouplingSocketMessage("Users viewing this page: 1", expectedOnlinePlayerList(username).toSet()),
//                    CouplingSocketMessage("Users viewing this page: 2", expectedOnlinePlayerList(username).toSet())
//                )
//            )
//    } teardown { result ->
//        result.forEach { it.second.close() }
//    }
//
//    @Test
//    fun whenPairsAreSavedWillSendMessageToClients() = asyncSetup(sdkContext {
//        object : SdkContext by it {
//            val tribe = stubTribe()
//            val sockets = mutableListOf<Pair<MutableList<String>, WS>>()
//            val expectedPairDoc = stubPairAssignmentDoc()
//        }
//    }) {
//        sdk.save(tribe)
//        sockets.add(openSocket(tribe, getCookieString(sdk)).await())
//    } exercise {
//        sdk.save(tribe.id.with(expectedPairDoc))
//    } verifyAnd {
//        sockets[0].first.map(String::toMessage)
//            .assertIsEqualTo(
//                listOf(
//                    CouplingSocketMessage("Users viewing this page: 1", expectedOnlinePlayerList(username).toSet()),
//                    PairAssignmentAdjustmentMessage(expectedPairDoc)
//                )
//            )
//    } teardown {
//        sockets.forEach { it.second.close() }
//    }
//
//    @Test
//    fun whenConnectionClosesOtherConnectionsGetMessageWithNewCount() = asyncSetup(sdkContext {
//        object : SdkContext by it {
//            val tribe = stubTribe()
//        }
//    }) {
//        sdk.save(tribe)
//    } exercise {
//        val cookieString = getCookieString(sdk)
//        val socketToClose = openSocket(tribe, cookieString).await()
//        openSocket(tribe, cookieString).await()
//            .also {
//                val deferred = CompletableDeferred<Unit>()
//                it.second.on("message") {
//                    deferred.complete(Unit)
//                }
//                socketToClose.second.close()
//                deferred.await()
//            }
//    } verifyAnd { openSocket ->
//        openSocket.first.map(String::toCouplingServerMessage)
//            .assertIsEqualTo(
//                listOf(
//                    CouplingSocketMessage("Users viewing this page: 2", expectedOnlinePlayerList(username).toSet()),
//                    CouplingSocketMessage("Users viewing this page: 1", expectedOnlinePlayerList(username).toSet())
//                )
//            )
//    } teardown { openSocket ->
//        openSocket.second.close()
//    }
//
//    @Test
//    fun whenNotAuthenticatedDoesNotTalkToYou() = asyncSetup(sdkContext { it }
//    ) exercise {
//        val host = process.env.WEBSOCKET_HOST.unsafeCast<String>()
//        val url = "wss://$host/api/${TribeId("whoops").value}/pairAssignments/current"
//        val socket = newWebsocket(url, json())
//        CompletableDeferred<Unit>().also { deferred ->
//            socket.on("close") { deferred.complete(Unit) }
//        }
//    } verify { deferred ->
//        withTimeout(100) {
//            deferred.await()
//        }
//    }
//
//    @Test
//    fun whenNotAuthorizedForTheTribeWillNotTalkToYou() = asyncSetup(sdkContext { it }
//    ) exercise {
//        val socket = connectToSocket(stubTribe().id, getCookieString(sdk))
//        CompletableDeferred<Unit>().also { deferred ->
//            socket.on("close") { deferred.complete(Unit) }
//        }
//    } verify { deferred ->
//        withTimeout(200) {
//            deferred.await()
//        }
//    }
//
//    @Test
//    fun willNotCrashWhenGoingToNonExistingSocketLocation() = asyncSetup(sdkContext { it }
//    ) exercise {
//        val host = process.env.WEBSOCKET_HOST.unsafeCast<String>()
//        val url = "wss://$host/api/404WTF"
//        val socket = newWebsocket(url, json())
//        CompletableDeferred<Unit>().also { deferred ->
//            socket.on("close") { deferred.complete(Unit) }
//        }
//    } verify { deferred ->
//        withTimeout(100) {
//            deferred.await()
//        }
//    }
//
//    @Test
//    fun whenSocketIsImmediatelyClosedDoesNotCrashServer() = asyncSetup(sdkContext {
//        object : SdkContext by it {
//            val tribe = stubTribe()
//        }
//    }) {
//        sdk.save(tribe)
//    } exercise {
//        val socket = connectToSocket(tribe.id, getCookieString(sdk))
//        val messageDeferred = CompletableDeferred<Unit>()
//        socket.on("open") {
//            socket.close()
//        }
//        socket.on("close") {
//            messageDeferred.complete(Unit)
//        }
//        messageDeferred
//    } verify { deferred ->
//        withTimeout(100) {
//            deferred.await()
//        }
//    }

    private fun openSocket(
        tribe: Tribe,
        cookieString: String,
        parent: Job? = null
    ): Deferred<Pair<MutableList<String>, WS>> {
        val socket = connectToSocket(tribe.id, cookieString)
        val messageDeferred = CompletableDeferred<Pair<MutableList<String>, WS>>(parent)
        val messages = mutableListOf<String>()
        socket.on("message") {
            messages.add(it)
            if(!messageDeferred.isCompleted)
                messageDeferred.complete(Pair(messages, socket))
        }
        return messageDeferred
    }

    private fun connectToSocket(tribeId: TribeId, cookieStringSync: String): WS {
        val host = process.env.WEBSOCKET_HOST.unsafeCast<String>()
        val url = "wss://$host/api/websocket?tribeId=${tribeId.value}"
        return newWebsocket(url, json("headers" to json("cookie" to cookieStringSync)))
    }

    private suspend fun getCookieString(sdk: AuthorizedKtorSdk): String {
        val baseUrl = URL("${process.env.BASEURL}")
        val cookiesUrl = Url(baseUrl.toString())
        return sdk.client.cookies(cookiesUrl).joinToString(";", transform = ::renderCookieHeader)
    }

}

private fun String.toCouplingServerMessage(): CouplingSocketMessage =
    fromJsonString<JsonCouplingSocketMessage>().toModel()

fun String.toMessage(): Message = fromJsonString<JsonMessage>().toModel()

private fun expectedOnlinePlayerList(username: String) =
    listOf(Player(email = "$username._temp", name = "", id = "-1"))
