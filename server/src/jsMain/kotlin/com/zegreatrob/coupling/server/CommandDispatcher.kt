package com.zegreatrob.coupling.server

import com.benasher44.uuid.Uuid
import com.zegreatrob.coupling.action.DispatchingActionExecutor
import com.zegreatrob.coupling.action.LoggingActionPipe
import com.zegreatrob.coupling.action.TraceIdProvider
import com.zegreatrob.coupling.action.pairassignmentdocument.AssignPinsAction
import com.zegreatrob.coupling.model.Message
import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.repository.BoostRepository
import com.zegreatrob.coupling.repository.dynamo.external.awsgatewaymanagement.ApiGatewayManagementApiClient
import com.zegreatrob.coupling.server.action.BroadcastAction
import com.zegreatrob.coupling.server.action.CannonProvider
import com.zegreatrob.coupling.server.action.GlobalStatsQuery
import com.zegreatrob.coupling.server.action.SecretGenerator
import com.zegreatrob.coupling.server.action.ServerCreateSecretCommandDispatcher
import com.zegreatrob.coupling.server.action.boost.ServerBoostQueryDispatcher
import com.zegreatrob.coupling.server.action.boost.ServerDeleteBoostCommandDispatcher
import com.zegreatrob.coupling.server.action.boost.ServerSaveBoostCommandDispatcher
import com.zegreatrob.coupling.server.action.connection.ConnectPartyUserCommand
import com.zegreatrob.coupling.server.action.connection.ConnectionsQuery
import com.zegreatrob.coupling.server.action.connection.DisconnectPartyUserCommand
import com.zegreatrob.coupling.server.action.connection.ReportDocCommand
import com.zegreatrob.coupling.server.action.pairassignmentdocument.CreatePairCandidateReportListAction
import com.zegreatrob.coupling.server.action.pairassignmentdocument.CurrentPairAssignmentDocumentQuery
import com.zegreatrob.coupling.server.action.pairassignmentdocument.NextPlayerAction
import com.zegreatrob.coupling.server.action.pairassignmentdocument.PairAssignmentDocumentListQuery
import com.zegreatrob.coupling.server.action.pairassignmentdocument.ServerDeletePairAssignmentsCommandDispatcher
import com.zegreatrob.coupling.server.action.pairassignmentdocument.ServerSavePairAssignmentDocumentCommandDispatcher
import com.zegreatrob.coupling.server.action.pairassignmentdocument.ServerSpinCommandDispatcher
import com.zegreatrob.coupling.server.action.pairassignmentdocument.ShufflePairsAction
import com.zegreatrob.coupling.server.action.party.PartyIntegrationQuery
import com.zegreatrob.coupling.server.action.party.ServerDeletePartyCommandDispatcher
import com.zegreatrob.coupling.server.action.pin.PinsQuery
import com.zegreatrob.coupling.server.action.pin.ServerDeletePinCommandDispatcher
import com.zegreatrob.coupling.server.action.pin.ServerSavePinCommandDispatcher
import com.zegreatrob.coupling.server.action.player.PlayersQuery
import com.zegreatrob.coupling.server.action.player.RetiredPlayersQuery
import com.zegreatrob.coupling.server.action.player.ServerDeletePlayerCommandDispatcher
import com.zegreatrob.coupling.server.action.player.ServerSavePlayerCommandDispatcher
import com.zegreatrob.coupling.server.action.secret.SecretListQuery
import com.zegreatrob.coupling.server.action.slack.ServerGrantSlackAccessCommandDispatcher
import com.zegreatrob.coupling.server.action.slack.SlackRepository
import com.zegreatrob.coupling.server.action.user.UserQuery
import com.zegreatrob.coupling.server.entity.pairassignment.PairAssignmentDispatcher
import com.zegreatrob.coupling.server.entity.party.PartyDispatcher
import com.zegreatrob.coupling.server.entity.party.ScopeSyntax
import com.zegreatrob.coupling.server.entity.user.UserDispatcher
import com.zegreatrob.coupling.server.express.Config
import com.zegreatrob.coupling.server.secret.JwtSecretGenerator
import com.zegreatrob.coupling.server.secret.ServerDeleteSecretCommandDispatcher
import com.zegreatrob.coupling.server.slack.FetchSlackRepository
import com.zegreatrob.testmints.action.ActionCannon
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlin.js.json

interface ICommandDispatcher :
    TraceIdProvider,
    AwsManagementApiSyntax,
    AwsSocketCommunicator,
    ConnectPartyUserCommand.Dispatcher,
    ConnectionsQuery.Dispatcher,
    CurrentPairAssignmentDocumentQuery.Dispatcher,
    DisconnectPartyUserCommand.Dispatcher,
    GlobalStatsQuery.Dispatcher,
    PairAssignmentDocumentListQuery.Dispatcher,
    PartyDispatcher,
    PinsQuery.Dispatcher,
    PartyIntegrationQuery.Dispatcher,
    PlayersQuery.Dispatcher,
    ReportDocCommand.Dispatcher,
    RepositoryCatalog,
    RetiredPlayersQuery.Dispatcher,
    ServerGrantSlackAccessCommandDispatcher,
    ScopeSyntax,
    SecretListQuery.Dispatcher,
    UserDispatcher,
    UserQuery.Dispatcher

class CommandDispatcher(
    override val currentUser: User,
    private val repositoryCatalog: RepositoryCatalog,
    override val scope: CoroutineScope,
    override val traceId: Uuid,
    override val managementApiClient: ApiGatewayManagementApiClient = apiGatewayManagementApiClient(),
) : ICommandDispatcher,
    RepositoryCatalog by repositoryCatalog,
    TraceIdProvider,
    BroadcastAction.Dispatcher<ICommandDispatcher> {
    override val cannon: ActionCannon<ICommandDispatcher> = ActionCannon(this, LoggingActionPipe(traceId))

    override val slackRepository: SlackRepository by lazy { FetchSlackRepository() }

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

    private fun nonCachingPlayerQueryDispatcher() = object :
        PlayersQuery.Dispatcher,
        RepositoryCatalog by this {}

    private val playersQueryCache = mutableMapOf<PartyId, Deferred<List<PartyRecord<Player>>>>()

    override suspend fun perform(query: PlayersQuery) =
        playersQueryCache.getOrPut(query.partyId) { scope.async { nonCachingPlayerQueryDispatcher().perform(query) } }
            .await()
}

class CurrentPartyDispatcher(
    override val currentPartyId: PartyId,
    private val commandDispatcher: CommandDispatcher,
) :
    ICommandDispatcher by commandDispatcher,
    DispatchingActionExecutor<CurrentPartyDispatcher>,
    CreatePairCandidateReportListAction.Dispatcher<CurrentPartyDispatcher>,
    ShufflePairsAction.Dispatcher<CurrentPartyDispatcher>,
    AssignPinsAction.Dispatcher,
    NextPlayerAction.Dispatcher<CurrentPartyDispatcher>,
    PairAssignmentDispatcher<CurrentPartyDispatcher>,
    BroadcastAction.Dispatcher<CurrentPartyDispatcher>,
    ServerSpinCommandDispatcher<CurrentPartyDispatcher>,
    ServerSaveSlackIntegrationCommandDispatcher,
    ServerCreateSecretCommandDispatcher,
    ServerSavePlayerCommandDispatcher,
    ServerDeletePlayerCommandDispatcher,
    ServerDeleteSecretCommandDispatcher,
    RetiredPlayersQuery.Dispatcher,
    ServerSavePairAssignmentDocumentCommandDispatcher<CurrentPartyDispatcher>,
    ServerDeletePairAssignmentsCommandDispatcher,
    ServerDeletePartyCommandDispatcher,
    ServerDeletePinCommandDispatcher,
    ServerSavePinCommandDispatcher,
    CannonProvider<CurrentPartyDispatcher> {
    override val userId: String get() = commandDispatcher.userId
    override val cannon: ActionCannon<CurrentPartyDispatcher> = ActionCannon(this, LoggingActionPipe(traceId))
    suspend fun isAuthorized() = currentPartyId.validateAuthorized() != null
    override val actionDispatcher = this
    private suspend fun PartyId.validateAuthorized() = if (userIsAuthorized(this)) this else null

    private suspend fun userIsAuthorized(partyId: PartyId) = currentUser.authorizedPartyIds.contains(partyId) ||
        userIsAlsoPlayer()

    private suspend fun userIsAlsoPlayer() = players()
        .map { it.email }
        .contains(currentUser.email)

    private suspend fun players() = perform(PlayersQuery(currentPartyId)).map { it.data.element }
    override suspend fun sendMessageAndReturnIdWhenFail(connectionId: String, message: Message): String? =
        commandDispatcher.sendMessageAndReturnIdWhenFail(connectionId, message)

    override val secretGenerator: SecretGenerator = object : JwtSecretGenerator {
        override val secretIssuer: String = Config.publicUrl
        override val secretAudience = "${Config.publicUrl}/api"
        override val secretSigningSecret: String = Config.secretSigningSecret
    }
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
    ServerBoostQueryDispatcher,
    ServerDeleteBoostCommandDispatcher {
    override val boostRepository: BoostRepository
}
