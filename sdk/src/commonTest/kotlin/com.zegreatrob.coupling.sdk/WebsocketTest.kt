package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.JsonCouplingSocketMessage
import com.zegreatrob.coupling.json.JsonMessage
import com.zegreatrob.coupling.json.fromJsonString
import com.zegreatrob.coupling.json.toModel
import com.zegreatrob.coupling.model.CouplingSocketMessage
import com.zegreatrob.coupling.model.Message
import com.zegreatrob.coupling.model.PairAssignmentAdjustmentMessage
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.stubmodel.stubPairAssignmentDoc
import com.zegreatrob.coupling.stubmodel.stubTribe
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minassert.assertIsNotEqualTo
import io.ktor.client.features.websocket.*
import io.ktor.client.request.*
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.withTimeout
import kotlin.test.Test

class WebsocketTest {

    private val socketHost = "socket.localhost"

    @Test
    fun whenOnlyOneConnectionWillReturnCountOfOne() = sdkSetup({
        object : SdkContext by it {
            val tribe = stubTribe()
        }
    }) {
        sdk.tribeRepository.save(tribe)
    } exercise {
        val webSocketSession = couplingSocketSession(tribe.id)
        val message = (webSocketSession.incoming.receive() as? Frame.Text)
        webSocketSession to message
    } verifyAnd { (_, message) ->
        message?.readText()?.toCouplingServerMessage()
            .assertIsEqualTo(
                CouplingSocketMessage("Users viewing this page: 1", expectedOnlinePlayerList(username).toSet(), null)
            )
    } teardown { (session) ->
        session.close()
        sdk.tribeRepository.delete(tribe.id)
    }

    private suspend fun SdkContext.couplingSocketSession(tribeId: TribeId) = generalPurposeClient.webSocketSession {
        url("wss://$socketHost/api/websocket?tribeId=${tribeId.value}&token=${sdk.token}")
    }

    @Test
    fun whenMultipleConnectionsWillReturnTheTotalCount() = sdkSetup({
        object : SdkContext by it {
            val tribe = stubTribe()
        }
    }) {
        sdk.tribeRepository.save(tribe)
    } exercise {
        val twoSessions = listOf(
            couplingSocketSession(tribe.id),
            couplingSocketSession(tribe.id),
        )
        twoSessions.forEach { it.incoming.receive() }

        val thirdSocket = couplingSocketSession(tribe.id)
        val thirdSocketMessage = thirdSocket.incoming.receive() as? Frame.Text
        (twoSessions + thirdSocket) to thirdSocketMessage
    } verifyAnd { (_, thirdSocketMessage) ->
        thirdSocketMessage
            ?.readText()
            ?.toCouplingServerMessage()
            .assertIsEqualTo(
                CouplingSocketMessage(
                    "Users viewing this page: 3", expectedOnlinePlayerList(username).toSet(), null
                )
            )
    } teardown { (result) ->
        result.forEach { it.close() }
        sdk.tribeRepository.delete(tribe.id)
    }

    @Test
    fun whenNewConnectionIsOpenExistingConnectionsReceiveMessage() = sdkSetup({
        object : SdkContext by it {
            val tribe = stubTribe()
        }
    }) {
        sdk.tribeRepository.save(tribe)
    } exercise {
        val socket1 = couplingSocketSession(tribe.id).alsoWaitForFirstFrame()
        val socket2 = couplingSocketSession(tribe.id).alsoWaitForFirstFrame()
        listOf(socket1, socket2)
    } verifyAnd { (socket1) ->
        socket1
            .readTextFrame()
            ?.toCouplingServerMessage()
            .assertIsEqualTo(
                CouplingSocketMessage("Users viewing this page: 2", expectedOnlinePlayerList(username).toSet())
            )
    } teardown { result ->
        result.forEach { it.close() }
        sdk.tribeRepository.delete(tribe.id)
    }

    private fun DefaultClientWebSocketSession.readAllAvailableMessages() = generateSequence {
        with(incoming.tryReceive()) {
            if (!isSuccess) null else getOrThrow() as? Frame.Text
        }
    }.map(Frame.Text::readText)
        .toList()

    private suspend fun DefaultClientWebSocketSession.readTextFrame() = (incoming.receive() as? Frame.Text)?.readText()

    @Test
    fun whenPairsAreSavedWillSendMessageToClients() = sdkSetup({
        object : SdkContext by it {
            val tribe = stubTribe()
            val sockets = mutableListOf<DefaultClientWebSocketSession>()
            val expectedPairDoc = stubPairAssignmentDoc()
        }
    }) {
        sdk.tribeRepository.save(tribe)
        sockets.add(couplingSocketSession(tribe.id).alsoWaitForFirstFrame())
    } exercise {
        sdk.pairAssignmentDocumentRepository.save(tribe.id.with(expectedPairDoc))
    } verifyAnd {
        sockets.first()
            .readTextFrame()
            ?.toMessage()
            .assertIsEqualTo(PairAssignmentAdjustmentMessage(expectedPairDoc))
    } teardown {
        sockets.forEach { it.close() }
        sdk.tribeRepository.delete(tribe.id)
    }

    private suspend fun DefaultClientWebSocketSession.alsoWaitForFirstFrame() = also {
        incoming.receive()
    }

    @Test
    fun whenConnectionClosesOtherConnectionsGetMessageWithNewCount() = sdkSetup({
        object : SdkContext by it {
            val tribe = stubTribe()
        }
    }) {
        sdk.tribeRepository.save(tribe)
    } exercise {
        val socketToClose = couplingSocketSession(tribe.id)
            .alsoWaitForFirstFrame()

        couplingSocketSession(tribe.id)
            .alsoWaitForFirstFrame()
            .also { socketToClose.close() }
    } verifyAnd { openSocket ->
        openSocket
            .readTextFrame()
            ?.toCouplingServerMessage()
            .assertIsEqualTo(
                CouplingSocketMessage("Users viewing this page: 1", expectedOnlinePlayerList(username).toSet())
            )
    } teardown { openSocket ->
        openSocket.close()
        sdk.tribeRepository.delete(tribe.id)
    }

    @Test
    fun whenNotAuthenticatedDoesNotTalkToYou() = sdkSetup({ it }
    ) exercise {
        val url = "wss://$socketHost/api/${TribeId("whoops").value}/pairAssignments/current"
        generalPurposeClient.webSocketSession { url(url) }
    } verify { socket ->
        withTimeout(400) {
            (socket.incoming.receive() as? Frame.Close)
                .assertIsNotEqualTo(null)
        }
    }

    @Test
    fun whenNotAuthorizedForTheTribeWillNotTalkToYou() = sdkSetup({ it }
    ) exercise {
        couplingSocketSession(stubTribe().id)
    } verify { socket ->
        withTimeout(400) {
            (socket.incoming.receive() as? Frame.Close)
                .assertIsNotEqualTo(null)
        }
    }

    @Test
    fun willNotCrashWhenGoingToNonExistingSocketLocation() = sdkSetup({ it }
    ) exercise {
        val url = "wss://$socketHost/api/404WTF"
        generalPurposeClient.webSocketSession { url(url) }
    } verify { socket ->
        withTimeout(400) {
            (socket.incoming.receive() as? Frame.Close)
                .assertIsNotEqualTo(null)
        }
    }

    @Test
    fun whenSocketIsImmediatelyClosedDoesNotCrashServer() = sdkSetup({
        object : SdkContext by it {
            val tribe = stubTribe()
        }
    }) {
        sdk.tribeRepository.save(tribe)
    } exercise {
        couplingSocketSession(tribe.id)
            .apply { close() }
    } verifyAnd { socket ->
        withTimeout(400) {
            (socket.incoming.receive() as? Frame.Close)
                .assertIsNotEqualTo(null)
        }
    } teardown {
        sdk.tribeRepository.delete(tribe.id)
    }

}

private fun String.toCouplingServerMessage(): CouplingSocketMessage =
    fromJsonString<JsonCouplingSocketMessage>().toModel()

fun String.toMessage(): Message = fromJsonString<JsonMessage>().toModel()

private fun expectedOnlinePlayerList(email: String) = listOf(
    Player(email = email, name = email.substring(0, email.indexOf("@")), id = "-1")
)
