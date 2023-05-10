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
import com.zegreatrob.coupling.server.action.boost.ServerDeleteBoostCommandDispatcher
import com.zegreatrob.coupling.server.action.boost.ServerSaveBoostCommandDispatcher
import com.zegreatrob.coupling.server.action.connection.ConnectPartyUserCommandDispatcher
import com.zegreatrob.coupling.server.action.connection.ConnectionsQueryDispatcher
import com.zegreatrob.coupling.server.action.connection.CurrentPartyIdSyntax
import com.zegreatrob.coupling.server.action.connection.DeletePartyCommandDispatcher
import com.zegreatrob.coupling.server.action.connection.DisconnectPartyUserCommandDispatcher
import com.zegreatrob.coupling.server.action.connection.ReportDocCommandDispatcher
import com.zegreatrob.coupling.server.action.pairassignmentdocument.CurrentPairAssignmentDocumentQueryDispatcher
import com.zegreatrob.coupling.server.action.pairassignmentdocument.DeletePairAssignmentDocumentCommandDispatcher
import com.zegreatrob.coupling.server.action.pairassignmentdocument.PairAssignmentDocumentListQueryDispatcher
import com.zegreatrob.coupling.server.action.pairassignmentdocument.ServerProposeNewPairsCommandDispatcher
import com.zegreatrob.coupling.server.action.pairassignmentdocument.ServerSavePairAssignmentDocumentCommandDispatcher
import com.zegreatrob.coupling.server.action.pin.DeletePinCommandDispatcher
import com.zegreatrob.coupling.server.action.pin.PinsQueryDispatcher
import com.zegreatrob.coupling.server.action.pin.SavePinCommandDispatcher
import com.zegreatrob.coupling.server.action.player.DeletePlayerCommandDispatcher
import com.zegreatrob.coupling.server.action.player.PlayersQuery
import com.zegreatrob.coupling.server.action.player.PlayersQueryDispatcher
import com.zegreatrob.coupling.server.action.player.RetiredPlayersQueryDispatcher
import com.zegreatrob.coupling.server.action.player.SavePlayerCommandDispatcher
import com.zegreatrob.coupling.server.action.user.UserQueryDispatcher
import com.zegreatrob.coupling.server.entity.pairassignment.PairAssignmentDispatcher
import com.zegreatrob.coupling.server.entity.party.PartyDispatcher
import com.zegreatrob.coupling.server.entity.party.ScopeSyntax
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
    override val managementApiClient: ApiGatewayManagementApiClient = apiGatewayManagementApiClient(),
) : ICommandDispatcher, RepositoryCatalog by repositoryCatalog {
    override val execute = this
    override val actionDispatcher = this

    private var authorizedPartyIdDispatcherJob: Deferred<CurrentPartyDispatcher>? = null

    suspend fun authorizedPartyIdDispatcher(partyId: String): CurrentPartyDispatcher {
        val preexistingJob = authorizedPartyIdDispatcherJob
        return preexistingJob?.await()
            ?: scope.async {
                CurrentPartyDispatcher(PartyId(partyId), this@CommandDispatcher)
            }.also {
                authorizedPartyIdDispatcherJob = it
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
    ServerSavePairAssignmentDocumentCommandDispatcher,
    DeletePairAssignmentDocumentCommandDispatcher,
    DeletePartyCommandDispatcher,
    DeletePinCommandDispatcher,
    SavePinCommandDispatcher,
    CurrentPairAssignmentDocumentQueryDispatcher,
    ServerProposeNewPairsCommandDispatcher,
    PairAssignmentDocumentListQueryDispatcher

class CurrentPartyDispatcher(
    override val currentPartyId: PartyId,
    private val commandDispatcher: CommandDispatcher,
) :
    ICommandDispatcher by commandDispatcher,
    PinsQueryDispatcher,
    PlayersQueryDispatcher,
    SavePlayerCommandDispatcher,
    DeletePlayerCommandDispatcher,
    RetiredPlayersQueryDispatcher,
    ServerSavePairAssignmentDocumentCommandDispatcher,
    DeletePairAssignmentDocumentCommandDispatcher,
    DeletePartyCommandDispatcher,
    DeletePinCommandDispatcher,
    SavePinCommandDispatcher,
    CurrentPairAssignmentDocumentQueryDispatcher,
    ServerProposeNewPairsCommandDispatcher,
    PairAssignmentDocumentListQueryDispatcher,
    ICurrentPartyDispatcher {
    override val userId: String get() = commandDispatcher.userId

    suspend fun isAuthorized() = currentPartyId.validateAuthorized() != null

    private suspend fun PartyId.validateAuthorized() = if (userIsAuthorized(this)) this else null

    private fun nonCachingPlayerQueryDispatcher() = object :
        PlayersQueryDispatcher,
        LoggingActionExecuteSyntax by this,
        CurrentPartyIdSyntax by this,
        RepositoryCatalog by this {}

    private val playerDeferred = scope.async(start = CoroutineStart.LAZY) {
        with(nonCachingPlayerQueryDispatcher()) {
            perform(PlayersQuery)
        }
    }

    override suspend fun perform(query: PlayersQuery) = playerDeferred.await()

    private suspend fun userIsAuthorized(partyId: PartyId) = user.authorizedPartyIds.contains(partyId) ||
        userIsAlsoPlayer()

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
        "endpoint" to Config.apiGatewayManagementApiHost,
    ).add(
        if (Process.getEnv("IS_OFFLINE") == "true") {
            json(
                "region" to "us-east-1",
                "credentials" to json(
                    "accessKeyId" to "lol",
                    "secretAccessKey" to "lol",
                ),
            )
        } else {
            json()
        },
    ),
)

interface PrereleaseDispatcher :
    ICommandDispatcher,
    ServerSaveBoostCommandDispatcher,
    BoostQueryDispatcher,
    ServerDeleteBoostCommandDispatcher {
    override val boostRepository: BoostRepository
}
