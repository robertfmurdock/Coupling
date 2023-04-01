package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.JsonCouplingSocketMessage
import com.zegreatrob.coupling.json.JsonMessage
import com.zegreatrob.coupling.json.fromJsonString
import com.zegreatrob.coupling.json.toModel
import com.zegreatrob.coupling.model.CouplingSocketMessage
import com.zegreatrob.coupling.model.Message
import com.zegreatrob.coupling.model.PairAssignmentAdjustmentMessage
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.stubmodel.stubPairAssignmentDoc
import com.zegreatrob.coupling.stubmodel.stubParty
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minassert.assertIsNotEqualTo
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.url
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.withTimeout
import kotlin.test.Test

class WebsocketTest {

    private val socketHost = "socket.localhost"

    @Test
    fun whenOnlyOneConnectionWillReturnCountOfOne() = sdkSetup.with({
        object : SdkContext by it {
            val party = stubParty()
        }
    }) {
        sdk.partyRepository.save(party)
    } exercise {
        val webSocketSession = couplingSocketSession(party.id)
        val message = (webSocketSession.incoming.receive() as? Frame.Text)
        webSocketSession to message
    } verifyAnd { (_, message) ->
        message?.readText()?.toCouplingServerMessage()
            .assertIsEqualTo(
                CouplingSocketMessage("Users viewing this page: 1", expectedOnlinePlayerList(username).toSet(), null),
            )
    } teardown { result ->
        result?.let { (session) ->
            session.close()
            sdk.partyRepository.deleteIt(party.id)
        }
    }

    private suspend fun SdkContext.couplingSocketSession(partyId: PartyId): DefaultClientWebSocketSession {
        val token = sdk.getToken()
        return generalPurposeClient.webSocketSession {
            url("wss://$socketHost/api/websocket?partyId=${partyId.value}&token=$token")
        }
    }

    @Test
    fun whenMultipleConnectionsWillReturnTheTotalCount() = sdkSetup.with({
        object : SdkContext by it {
            val party = stubParty()
        }
    }) {
        sdk.partyRepository.save(party)
    } exercise {
        val twoSessions = listOf(
            couplingSocketSession(party.id),
            couplingSocketSession(party.id),
        )
        twoSessions.forEach { it.incoming.receive() }

        val thirdSocket = couplingSocketSession(party.id)
        val thirdSocketMessage = thirdSocket.incoming.receive() as? Frame.Text
        (twoSessions + thirdSocket) to thirdSocketMessage
    } verifyAnd { (_, thirdSocketMessage) ->
        thirdSocketMessage
            ?.readText()
            ?.toCouplingServerMessage()
            .assertIsEqualTo(
                CouplingSocketMessage(
                    "Users viewing this page: 3",
                    expectedOnlinePlayerList(username).toSet(),
                    null,
                ),
            )
    } teardown { result ->
        result?.let { (session) ->
            session.forEach { it.close() }
            sdk.partyRepository.deleteIt(party.id)
        }
    }

    @Test
    fun whenNewConnectionIsOpenExistingConnectionsReceiveMessage() = sdkSetup.with({
        object : SdkContext by it {
            val party = stubParty()
        }
    }) {
        sdk.partyRepository.save(party)
    } exercise {
        val socket1 = couplingSocketSession(party.id).alsoWaitForFirstFrame()
        val socket2 = couplingSocketSession(party.id).alsoWaitForFirstFrame()
        listOf(socket1, socket2)
    } verifyAnd { (socket1) ->
        socket1
            .readTextFrame()
            ?.toCouplingServerMessage()
            .assertIsEqualTo(
                CouplingSocketMessage("Users viewing this page: 2", expectedOnlinePlayerList(username).toSet()),
            )
    } teardown { result ->
        result?.forEach { it.close() }
        sdk.partyRepository.deleteIt(party.id)
    }

    private suspend fun DefaultClientWebSocketSession.readTextFrame() = (incoming.receive() as? Frame.Text)?.readText()

    @Test
    fun whenPairsAreSavedWillSendMessageToClients() = sdkSetup.with({
        object : SdkContext by it {
            val party = stubParty()
            val sockets = mutableListOf<DefaultClientWebSocketSession>()
            val expectedPairDoc = stubPairAssignmentDoc()
        }
    }) {
        sdk.partyRepository.save(party)
        sockets.add(couplingSocketSession(party.id).alsoWaitForFirstFrame())
    } exercise {
        sdk.pairAssignmentDocumentRepository.save(party.id.with(expectedPairDoc))
    } verifyAnd {
        sockets.first()
            .readTextFrame()
            ?.toMessage()
            .assertIsEqualTo(PairAssignmentAdjustmentMessage(expectedPairDoc))
    } teardown {
        sockets.forEach { it.close() }
        sdk.partyRepository.deleteIt(party.id)
    }

    private suspend fun DefaultClientWebSocketSession.alsoWaitForFirstFrame() = also {
        incoming.receive()
    }

    @Test
    fun whenConnectionClosesOtherConnectionsGetMessageWithNewCount() = sdkSetup.with({
        object : SdkContext by it {
            val party = stubParty()
        }
    }) {
        sdk.partyRepository.save(party)
    } exercise {
        val socketToClose = couplingSocketSession(party.id)
            .alsoWaitForFirstFrame()

        couplingSocketSession(party.id)
            .alsoWaitForFirstFrame()
            .also { socketToClose.close() }
    } verifyAnd { openSocket ->
        openSocket
            .readTextFrame()
            ?.toCouplingServerMessage()
            .assertIsEqualTo(
                CouplingSocketMessage("Users viewing this page: 1", expectedOnlinePlayerList(username).toSet()),
            )
    } teardown { openSocket ->
        openSocket?.close()
        sdk.partyRepository.deleteIt(party.id)
    }

    @Test
    fun whenNotAuthenticatedDoesNotTalkToYou() = sdkSetup() exercise {
        val url = "wss://$socketHost/api/${PartyId("whoops").value}/pairAssignments/current"
        runCatching {
            generalPurposeClient.webSocketSession {
                url(url)
            }
        }
    } verify { result ->
        result.getOrNull()
            .assertIsEqualTo(null)
    }

    @Test
    fun whenNotAuthorizedForThePartyWillNotTalkToYou() = sdkSetup() exercise {
        runCatching { couplingSocketSession(stubParty().id) }
    } verify { result ->
        result.getOrNull()
            .assertIsEqualTo(null)
    }

    @Test
    fun willNotCrashWhenGoingToNonExistingSocketLocation() = sdkSetup() exercise {
        val url = "wss://$socketHost/api/404WTF"
        runCatching { generalPurposeClient.webSocketSession { url(url) } }
    } verify { result ->
        result.getOrNull()
            .assertIsEqualTo(null)
    }

    @Test
    fun whenSocketIsImmediatelyClosedDoesNotCrashServer() = sdkSetup.with({
        object : SdkContext by it {
            val party = stubParty()
        }
    }) {
        sdk.partyRepository.save(party)
    } exercise {
        couplingSocketSession(party.id)
            .apply { close() }
    } verifyAnd { socket ->
        withTimeout(400) {
            (socket.incoming.receive() as? Frame.Close)
                .assertIsNotEqualTo(null)
        }
    } teardown {
        sdk.partyRepository.deleteIt(party.id)
    }
}

private fun String.toCouplingServerMessage(): CouplingSocketMessage =
    fromJsonString<JsonCouplingSocketMessage>().toModel()

fun String.toMessage(): Message = fromJsonString<JsonMessage>().toModel()

private fun expectedOnlinePlayerList(email: String) = listOf(
    Player(email = email, name = email.substring(0, email.indexOf("@")), id = "-1"),
)
