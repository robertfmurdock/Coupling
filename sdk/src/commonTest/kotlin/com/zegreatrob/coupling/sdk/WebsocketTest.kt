package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.pairassignmentdocument.SavePairAssignmentsCommand
import com.zegreatrob.coupling.action.pairassignmentdocument.fire
import com.zegreatrob.coupling.action.party.DeletePartyCommand
import com.zegreatrob.coupling.action.party.SavePartyCommand
import com.zegreatrob.coupling.action.party.fire
import com.zegreatrob.coupling.json.JsonCouplingSocketMessage
import com.zegreatrob.coupling.json.JsonMessage
import com.zegreatrob.coupling.json.fromJsonString
import com.zegreatrob.coupling.json.toModel
import com.zegreatrob.coupling.model.CouplingSocketMessage
import com.zegreatrob.coupling.model.Message
import com.zegreatrob.coupling.model.PairAssignmentAdjustmentMessage
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.stubmodel.stubPairAssignmentDoc
import com.zegreatrob.coupling.stubmodel.stubPartyDetails
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.action.DispatcherPipeCannon
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.url
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import kotlin.test.Test

class WebsocketTest {

    private val socketHost = "socket.localhost"

    @Test
    fun whenOnlyOneConnectionWillReturnCountOfOne() = asyncSetup.with({
        object : SdkContext by it {
            val party = stubPartyDetails()
        }
    }) {
        sdk.fire(SavePartyCommand(party))
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
            sdk.fire(DeletePartyCommand(party.id))
        }
    }

    private suspend fun SdkContext.couplingSocketSession(partyId: PartyId): DefaultClientWebSocketSession {
        val token = ((sdk as DispatcherPipeCannon).dispatcher as KtorCouplingSdkDispatcher).getIdTokenFunc()
        return generalPurposeClient.webSocketSession {
            url("wss://$socketHost/api/websocket?partyId=${partyId.value}&token=$token")
        }
    }

    @Test
    fun whenMultipleConnectionsWillReturnTheTotalCount() = asyncSetup.with({
        object : SdkContext by it {
            val party = stubPartyDetails()
        }
    }) {
        sdk.fire(SavePartyCommand(party))
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
            sdk.fire(DeletePartyCommand(party.id))
        }
    }

    @Test
    fun whenNewConnectionIsOpenExistingConnectionsReceiveMessage() = asyncSetup.with({
        object : SdkContext by it {
            val party = stubPartyDetails()
        }
    }) {
        sdk.fire(SavePartyCommand(party))
    } exercise {
        val socket1 = couplingSocketSession(party.id).alsoWaitForFirstFrame()
        val socket2 = couplingSocketSession(party.id).alsoWaitForFirstFrame()
        listOf(socket1, socket2)
    } verifyAnd { (socket1) ->
        runCatching { withTimeout(2000) { socket1.readTextFrame() } }
            .getOrNull()
            ?.toCouplingServerMessage()
            .assertIsEqualTo(
                CouplingSocketMessage("Users viewing this page: 2", expectedOnlinePlayerList(username).toSet()),
                "Did not receive appropriate message in 2 seconds",
            )
    } teardown { result ->
        result?.forEach { it.close() }
        sdk.fire(DeletePartyCommand(party.id))
    }

    private suspend fun DefaultClientWebSocketSession.readTextFrame() = (incoming.receive() as? Frame.Text)?.readText()

    @Test
    fun whenPairsAreSavedWillSendMessageToClients() = asyncSetup.with({
        object : SdkContext by it {
            val party = stubPartyDetails()
            val sockets = mutableListOf<DefaultClientWebSocketSession>()
            val expectedPairDoc = stubPairAssignmentDoc()
        }
    }) {
        sdk.fire(SavePartyCommand(party))
        sockets.add(couplingSocketSession(party.id).alsoWaitForFirstFrame())
    } exercise {
        sdk.fire(SavePairAssignmentsCommand(party.id, expectedPairDoc))
    } verifyAnd {
        sockets.first()
            .readTextFrame()
            ?.toMessage()
            .assertIsEqualTo(PairAssignmentAdjustmentMessage(expectedPairDoc))
    } teardown {
        sockets.forEach { it.close() }
        sdk.fire(DeletePartyCommand(party.id))
    }

    private suspend fun DefaultClientWebSocketSession.alsoWaitForFirstFrame() = also {
        incoming.receive()
    }

    @Test
    fun whenConnectionClosesOtherConnectionsGetMessageWithNewCount() = asyncSetup.with({
        object : SdkContext by it {
            val party = stubPartyDetails()
        }
    }) {
        sdk.fire(SavePartyCommand(party))
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
        sdk.fire(DeletePartyCommand(party.id))
    }

    @Test
    fun whenNotAuthenticatedDoesNotTalkToYou() = asyncSetup() exercise {
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
    fun whenNotAuthorizedForThePartyWillNotTalkToYou() = asyncSetup() exercise {
        runCatching { couplingSocketSession(stubPartyDetails().id) }
    } verify { result ->
        result.getOrNull()
            .assertIsEqualTo(null)
    }

    @Test
    fun willNotCrashWhenGoingToNonExistingSocketLocation() = asyncSetup() exercise {
        val url = "wss://$socketHost/api/404WTF"
        runCatching { generalPurposeClient.webSocketSession { url(url) } }
    } verify { result ->
        result.getOrNull()
            .assertIsEqualTo(null)
    }

    @Test
    fun whenSocketIsImmediatelyClosedDoesNotCrashServer() = asyncSetup.with({
        object : SdkContext by it {
            val party = stubPartyDetails()
        }
    }) {
        sdk.fire(SavePartyCommand(party))
    } exercise {
        couplingSocketSession(party.id)
            .apply { close() }
    } verifyAnd { socket ->
        withTimeout(400) {
            runCatching {
                var frame = socket.incoming.receive() as? Frame.Close
                while (frame == null) {
                    delay(50)
                    frame = socket.incoming.receive() as? Frame.Close
                }
                throw ClosedReceiveChannelException("received close frame.")
            }.exceptionOrNull()
                ?.let { it::class }
                .assertIsEqualTo(ClosedReceiveChannelException::class)
        }
    } teardown {
        sdk.fire(DeletePartyCommand(party.id))
    }
}

private fun String.toCouplingServerMessage(): CouplingSocketMessage =
    fromJsonString<JsonCouplingSocketMessage>().toModel()

fun String.toMessage(): Message = fromJsonString<JsonMessage>().toModel()

private fun expectedOnlinePlayerList(email: String) = listOf(
    Player(id = "-1", name = email.substring(0, email.indexOf("@")), email = email, avatarType = null),
)
