package com.zegreatrob.coupling.server

import AwsManagementApiSyntax
import AwsSocketCommunicator
import com.benasher44.uuid.Uuid
import com.zegreatrob.coupling.action.DispatchingActionExecutor
import com.zegreatrob.coupling.action.LoggingActionExecuteSyntax
import com.zegreatrob.coupling.dynamo.external.awsgatewaymanagement.ApiGatewayManagementApi
import com.zegreatrob.coupling.model.Message
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.server.action.BroadcastActionDispatcher
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
import com.zegreatrob.coupling.server.entity.tribe.TribeDispatcher
import com.zegreatrob.coupling.server.entity.user.UserDispatcher
import com.zegreatrob.coupling.server.express.Config
import com.zegreatrob.testmints.action.async.SuspendActionExecuteSyntax
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlin.js.json

interface ICommandDispatcher :
    LoggingActionExecuteSyntax,
    ScopeSyntax,
    TribeDispatcher,
    PairAssignmentDispatcher,
    UserDispatcher,
    UserQueryDispatcher,
    ConnectTribeUserCommandDispatcher,
    ConnectionsQueryDispatcher,
    DisconnectTribeUserCommandDispatcher,
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
    override val managementApi: ApiGatewayManagementApi = apiGatewayManagementApi()
) : ICommandDispatcher, RepositoryCatalog by repositoryCatalog {
    override val execute = this
    override val actionDispatcher = this

    private var authorizedTribeIdDispatcherJob: Deferred<CurrentTribeIdDispatcher>? = null

    suspend fun authorizedTribeIdDispatcher(tribeId: String): CurrentTribeIdDispatcher {
        val preexistingJob = authorizedTribeIdDispatcherJob
        return preexistingJob?.await()
            ?: scope.async {
                CurrentTribeIdDispatcher(TribeId(tribeId), this@CommandDispatcher)
            }.also {
                authorizedTribeIdDispatcherJob = it
            }.await()
    }

}

interface ICurrentTribeIdDispatcher :
    ICommandDispatcher,
    PinsQueryDispatcher,
    PlayersQueryDispatcher,
    SavePlayerCommandDispatcher,
    DeletePlayerCommandDispatcher,
    RetiredPlayersQueryDispatcher,
    SavePairAssignmentDocumentCommandDispatcher,
    DeletePairAssignmentDocumentCommandDispatcher,
    DeleteTribeCommandDispatcher,
    DeletePinCommandDispatcher,
    SavePinCommandDispatcher,
    CurrentPairAssignmentDocumentQueryDispatcher,
    ProposeNewPairsCommandDispatcher,
    PairAssignmentDocumentListQueryDispatcher

class CurrentTribeIdDispatcher(
    override val currentTribeId: TribeId,
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
    DeleteTribeCommandDispatcher,
    DeletePinCommandDispatcher,
    SavePinCommandDispatcher,
    CurrentPairAssignmentDocumentQueryDispatcher,
    ProposeNewPairsCommandDispatcher,
    PairAssignmentDocumentListQueryDispatcher,
    ICurrentTribeIdDispatcher {
    override val userId: String get() = commandDispatcher.userId

    suspend fun isAuthorized() = currentTribeId.validateAuthorized() != null

    private suspend fun TribeId.validateAuthorized() = if (userIsAuthorized(this)) this else null

    private fun nonCachingPlayerQueryDispatcher() = object : PlayersQueryDispatcher,
        LoggingActionExecuteSyntax by this,
        CurrentTribeIdSyntax by this,
        RepositoryCatalog by this {}

    private val playerDeferred = scope.async(start = CoroutineStart.LAZY) {
        with(nonCachingPlayerQueryDispatcher()) {
            perform(PlayersQuery)
        }
    }

    override suspend fun perform(query: PlayersQuery) = playerDeferred.await()

    private suspend fun userIsAuthorized(tribeId: TribeId) = user.authorizedTribeIds.contains(tribeId)
            || userIsAlsoPlayer()

    private suspend fun userIsAlsoPlayer() = players()
        .map { it.email }
        .contains(user.email)

    private suspend fun players() = playerDeferred.await().value.map { it.data.element }
    override suspend fun sendMessageAndReturnIdWhenFail(connectionId: String, message: Message): String? =
        commandDispatcher.sendMessageAndReturnIdWhenFail(connectionId, message)

}

fun apiGatewayManagementApi() = ApiGatewayManagementApi(
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

interface PrereleaseTribeIdDispatcher :
    SaveBoostCommandDispatcher,
    SuspendActionExecuteSyntax


