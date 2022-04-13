package com.zegreatrob.coupling.server

import AwsManagementApiSyntax
import AwsSocketCommunicator
import com.benasher44.uuid.Uuid
import com.zegreatrob.coupling.action.DispatchingActionExecutor
import com.zegreatrob.coupling.action.LoggingActionExecuteSyntax
import com.zegreatrob.coupling.model.Message
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.repository.BoostRepository
import com.zegreatrob.coupling.repository.dynamo.external.awsgatewaymanagement.ApiGatewayManagementApiClient
import com.zegreatrob.coupling.server.action.BroadcastActionDispatcher
import com.zegreatrob.coupling.server.action.boost.BoostQueryDispatcher
import com.zegreatrob.coupling.server.action.boost.DeleteBoostCommandDispatcher
import com.zegreatrob.coupling.server.action.boost.SaveBoostCommandDispatcher
import com.zegreatrob.coupling.server.action.connection.*
import com.zegreatrob.coupling.server.action.pairassignmentdocument.*
import com.zegreatrob.coupling.server.action.pin.DeletePinCommandDispatcher
import com.zegreatrob.coupling.server.action.pin.PinsQueryDispatcher
import com.zegreatrob.coupling.server.action.pin.SavePinCommandDispatcher
import com.zegreatrob.coupling.server.action.player.*
import com.zegreatrob.coupling.server.action.user.UserQueryDispatcher
import com.zegreatrob.coupling.server.entity.pairassignment.PairAssignmentDispatcher
import com.zegreatrob.coupling.server.entity.tribe.ScopeSyntax
import com.zegreatrob.coupling.server.entity.tribe.PartyDispatcher
import com.zegreatrob.coupling.server.entity.user.UserDispatcher
import com.zegreatrob.coupling.server.express.Config
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlin.js.json

interface ICommandDispatcher :
    LoggingActionExecuteSyntax,
    ScopeSyntax,
    PartyDispatcher,
    PairAssignmentDispatcher,
    UserDispatcher,
    UserQueryDispatcher,
    ConnectPartyUserCommandDispatcher,
    ConnectionsQueryDispatcher,
    DisconnectPartyUserCommandDispatcher,
    ReportDocCommandDispatcher,
    DispatchingActionExecutor<CommandDispatcher>,
    RepositoryCatalog,
    AwsManagementApiSyntax,
    BroadcastActionDispatcher,
    AwsSocketCommunicator

class CommandDispatcher(
    override val user: User,
    private val repositoryCatalog: RepositoryCatalog,
    override val scope: CoroutineScope,
    override val traceId: Uuid,
    override val managementApiClient: ApiGatewayManagementApiClient = apiGatewayManagementApiClient()
) : ICommandDispatcher, RepositoryCatalog by repositoryCatalog {
    override val execute = this
    override val actionDispatcher = this

    private var authorizedTribeIdDispatcherJob: Deferred<CurrentPartyDispatcher>? = null

    suspend fun authorizedTribeIdDispatcher(tribeId: String): CurrentPartyDispatcher {
        val preexistingJob = authorizedTribeIdDispatcherJob
        return preexistingJob?.await()
            ?: scope.async {
                CurrentPartyDispatcher(PartyId(tribeId), this@CommandDispatcher)
            }.also {
                authorizedTribeIdDispatcherJob = it
            }.await()
    }

}

interface ICurrentPartyDispatcher :
    ICommandDispatcher,
    PinsQueryDispatcher,
    PlayersQueryDispatcher,
    SavePlayerCommandDispatcher,
    DeletePlayerCommandDispatcher,
    RetiredPlayersQueryDispatcher,
    SavePairAssignmentDocumentCommandDispatcher,
    DeletePairAssignmentDocumentCommandDispatcher,
    DeletePartyCommandDispatcher,
    DeletePinCommandDispatcher,
    SavePinCommandDispatcher,
    CurrentPairAssignmentDocumentQueryDispatcher,
    ProposeNewPairsCommandDispatcher,
    PairAssignmentDocumentListQueryDispatcher

class CurrentPartyDispatcher(
    override val currentPartyId: PartyId,
    private val commandDispatcher: CommandDispatcher
) :
    ICommandDispatcher by commandDispatcher,
    PinsQueryDispatcher,
    PlayersQueryDispatcher,
    SavePlayerCommandDispatcher,
    DeletePlayerCommandDispatcher,
    RetiredPlayersQueryDispatcher,
    SavePairAssignmentDocumentCommandDispatcher,
    DeletePairAssignmentDocumentCommandDispatcher,
    DeletePartyCommandDispatcher,
    DeletePinCommandDispatcher,
    SavePinCommandDispatcher,
    CurrentPairAssignmentDocumentQueryDispatcher,
    ProposeNewPairsCommandDispatcher,
    PairAssignmentDocumentListQueryDispatcher,
    ICurrentPartyDispatcher {
    override val userId: String get() = commandDispatcher.userId

    suspend fun isAuthorized() = currentPartyId.validateAuthorized() != null

    private suspend fun PartyId.validateAuthorized() = if (userIsAuthorized(this)) this else null

    private fun nonCachingPlayerQueryDispatcher() = object : PlayersQueryDispatcher,
        LoggingActionExecuteSyntax by this,
        CurrentPartyIdSyntax by this,
        RepositoryCatalog by this {}

    private val playerDeferred = scope.async(start = CoroutineStart.LAZY) {
        with(nonCachingPlayerQueryDispatcher()) {
            perform(PlayersQuery)
        }
    }

    override suspend fun perform(query: PlayersQuery) = playerDeferred.await()

    private suspend fun userIsAuthorized(tribeId: PartyId) = user.authorizedPartyIds.contains(tribeId)
            || userIsAlsoPlayer()

    private suspend fun userIsAlsoPlayer() = players()
        .map { it.email }
        .contains(user.email)

    private suspend fun players() = playerDeferred.await().value.map { it.data.element }
    override suspend fun sendMessageAndReturnIdWhenFail(connectionId: String, message: Message): String? =
        commandDispatcher.sendMessageAndReturnIdWhenFail(connectionId, message)

}

fun apiGatewayManagementApiClient() = ApiGatewayManagementApiClient(
    json(
        "apiVersion" to "2018-11-29",
        "endpoint" to Config.apiGatewayManagementApiHost
    ).add(
        if (Process.getEnv("IS_OFFLINE") == "true")
            json(
                "region" to "us-east-1",
                "credentials" to json(
                    "accessKeyId" to "lol",
                    "secretAccessKey" to "lol"
                )
            )
        else
            json()
    )
)

interface PrereleaseDispatcher : ICommandDispatcher, SaveBoostCommandDispatcher, BoostQueryDispatcher,
    DeleteBoostCommandDispatcher {
    override val boostRepository: BoostRepository
}
