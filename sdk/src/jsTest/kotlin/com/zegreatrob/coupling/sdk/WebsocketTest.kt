package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.JsonCouplingSocketMessage
import com.zegreatrob.coupling.json.JsonMessage
import com.zegreatrob.coupling.json.fromJsonString
import com.zegreatrob.coupling.json.toModel
import com.zegreatrob.coupling.model.CouplingSocketMessage
import com.zegreatrob.coupling.model.Message
import com.zegreatrob.coupling.model.PairAssignmentAdjustmentMessage
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.stubmodel.stubPairAssignmentDoc
import com.zegreatrob.coupling.stubmodel.stubParty
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minassert.assertIsNotEqualTo
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.websocket.*
import kotlinx.coroutines.withTimeout
import kotlin.test.Test

class WebsocketTest {

    private val socketHost = "socket.localhost"

    @Test
    fun whenOnlyOneConnectionWillReturnCountOfOne() = sdkSetup.with({
        object : SdkContext by it {
            val tribe = stubParty()
        }
    }) {
        sdk.partyRepository.save(tribe)
    } exercise {
        val webSocketSession = couplingSocketSession(tribe.id)
        val message = (webSocketSession.incoming.receive() as? Frame.Text)
        webSocketSession to message
    } verifyAnd { (_, message) ->
        message?.readText()?.toCouplingServerMessage()
            .assertIsEqualTo(
                CouplingSocketMessage("Users viewing this page: 1", expectedOnlinePlayerList(username).toSet(), null)
            )
    } teardown { result ->
        result?.let { (session) ->
            session.close()
            sdk.partyRepository.delete(tribe.id)
        }
    }

    private suspend fun SdkContext.couplingSocketSession(partyId: PartyId): DefaultClientWebSocketSession {
        val token = sdk.getToken()
        return generalPurposeClient.webSocketSession {
            url("wss://$socketHost/api/websocket?tribeId=${partyId.value}&token=$token")
        }
    }

    @Test
    fun whenMultipleConnectionsWillReturnTheTotalCount() = sdkSetup.with({
        object : SdkContext by it {
            val tribe = stubParty()
        }
    }) {
        sdk.partyRepository.save(tribe)
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
    } teardown { result ->
        result?.let { (session) ->
            session.forEach { it.close() }
            sdk.partyRepository.delete(tribe.id)
        }
    }

    @Test
    fun whenNewConnectionIsOpenExistingConnectionsReceiveMessage() = sdkSetup.with({
        object : SdkContext by it {
            val tribe = stubParty()
        }
    }) {
        sdk.partyRepository.save(tribe)
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
        result?.forEach { it.close() }
        sdk.partyRepository.delete(tribe.id)
    }

    private suspend fun DefaultClientWebSocketSession.readTextFrame() = (incoming.receive() as? Frame.Text)?.readText()

    @Test
    fun whenPairsAreSavedWillSendMessageToClients() = sdkSetup.with({
        object : SdkContext by it {
            val tribe = stubParty()
            val sockets = mutableListOf<DefaultClientWebSocketSession>()
            val expectedPairDoc = stubPairAssignmentDoc()
        }
    }) {
        sdk.partyRepository.save(tribe)
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
        sdk.partyRepository.delete(tribe.id)
    }

    private suspend fun DefaultClientWebSocketSession.alsoWaitForFirstFrame() = also {
        incoming.receive()
    }

    @Test
    fun whenConnectionClosesOtherConnectionsGetMessageWithNewCount() = sdkSetup.with({
        object : SdkContext by it {
            val tribe = stubParty()
        }
    }) {
        sdk.partyRepository.save(tribe)
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
        openSocket?.close()
        sdk.partyRepository.delete(tribe.id)
    }

    @Test
    fun whenNotAuthenticatedDoesNotTalkToYou() = sdkSetup(
    ) exercise {
        val url = "wss://$socketHost/api/${PartyId("whoops").value}/pairAssignments/current"
        generalPurposeClient.webSocketSession { url(url) }
    } verify { socket ->
        withTimeout(400) {
            (socket.incoming.receive() as? Frame.Close)
                .assertIsNotEqualTo(null)
        }
    }

    @Test
    fun whenNotAuthorizedForTheTribeWillNotTalkToYou() = sdkSetup(
    ) exercise {
        couplingSocketSession(stubParty().id)
    } verify { socket ->
        withTimeout(400) {
            (socket.incoming.receive() as? Frame.Close)
                .assertIsNotEqualTo(null)
        }
    }

    @Test
    fun willNotCrashWhenGoingToNonExistingSocketLocation() = sdkSetup(
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
    fun whenSocketIsImmediatelyClosedDoesNotCrashServer() = sdkSetup.with({
        object : SdkContext by it {
            val tribe = stubParty()
        }
    }) {
        sdk.partyRepository.save(tribe)
    } exercise {
        couplingSocketSession(tribe.id)
            .apply { close() }
    } verifyAnd { socket ->
        withTimeout(400) {
            (socket.incoming.receive() as? Frame.Close)
                .assertIsNotEqualTo(null)
        }
    } teardown {
        sdk.partyRepository.delete(tribe.id)
    }

}

private fun String.toCouplingServerMessage(): CouplingSocketMessage =
    fromJsonString<JsonCouplingSocketMessage>().toModel()

fun String.toMessage(): Message = fromJsonString<JsonMessage>().toModel()

private fun expectedOnlinePlayerList(email: String) = listOf(
    Player(email = email, name = email.substring(0, email.indexOf("@")), id = "-1")
)
