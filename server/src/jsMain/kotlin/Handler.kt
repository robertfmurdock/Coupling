@file:Suppress("HttpUrlsUsage")

import com.zegreatrob.coupling.json.JsonMessage
import com.zegreatrob.coupling.json.fromJsonString
import com.zegreatrob.coupling.json.toJsonString
import com.zegreatrob.coupling.json.toModel
import com.zegreatrob.coupling.json.toSerializable
import com.zegreatrob.coupling.model.CouplingConnection
import com.zegreatrob.coupling.model.CouplingSocketMessage
import com.zegreatrob.coupling.model.PairAssignmentAdjustmentMessage
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.user.UserDetails
import com.zegreatrob.coupling.model.user.UserId
import com.zegreatrob.coupling.repository.dynamo.external.awsgatewaymanagement.ApiGatewayManagementApiClient
import com.zegreatrob.coupling.repository.dynamo.external.awsgatewaymanagement.DeleteConnectionCommand
import com.zegreatrob.coupling.repository.dynamo.external.awsgatewaymanagement.PostToConnectionCommand
import com.zegreatrob.coupling.server.CommandDispatcher
import com.zegreatrob.coupling.server.Process
import com.zegreatrob.coupling.server.action.BroadcastAction
import com.zegreatrob.coupling.server.action.connection.ConnectPartyUserCommand
import com.zegreatrob.coupling.server.action.connection.ConnectionsQuery
import com.zegreatrob.coupling.server.action.connection.DisconnectPartyUserCommand
import com.zegreatrob.coupling.server.action.connection.ReportDocCommand
import com.zegreatrob.coupling.server.apiGatewayManagementApiClient
import com.zegreatrob.coupling.server.buildApp
import com.zegreatrob.coupling.server.commandDispatcher
import com.zegreatrob.coupling.server.express.middleware.middleware
import com.zegreatrob.coupling.server.express.route.jwtMiddleware
import com.zegreatrob.coupling.server.express.route.userLoadingMiddleware
import com.zegreatrob.coupling.server.external.awssdk.clientlambda.InvokeCommand
import com.zegreatrob.coupling.server.external.awssdk.clientlambda.LambdaClient
import com.zegreatrob.coupling.server.external.express.Request
import com.zegreatrob.coupling.server.external.express.express
import com.zegreatrob.minjson.at
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.await
import kotlinx.coroutines.promise
import kotools.types.text.toNotBlankString
import kotlin.js.Json
import kotlin.js.Promise
import kotlin.js.json
import kotlin.uuid.Uuid

private val app by lazy {
    buildApp()
}

@JsExport
@JsName("serverless")
fun serverless(event: dynamic, context: dynamic): dynamic {
    event.path = if (event.path.unsafeCast<String?>() == "") "/" else event.path
    return js("require('serverless-http')")(app)(event, context)
}

private val websocketApp by lazy {
    express().apply {
        middleware()
        use(jwtMiddleware { request -> request.query["token"] })
        use(userLoadingMiddleware())

        all("*") { request, response, _ ->
            val connectionId = request.connectionId
            with(
                request.scope.async {
                    if (request.isAuthenticated != true) {
                        delete(connectionId, apiGatewayManagementApiClient()).await()
                        401
                    } else {
                        println("connect $connectionId")
                        handleConnect(request, connectionId, request.event)
                    }
                },
            ) {
                invokeOnCompletion { cause ->
                    if (cause != null) {
                        response.sendStatus(403)
                            .also { println("exception $cause") }
                            .also { cause.printStackTrace() }
                    } else {
                        response.sendStatus(getCompleted())
                    }
                }
            }
        }
    }
}

@Suppress("unused")
@JsExport
@JsName("serverlessSocketConnect")
fun serverlessSocketConnect(event: dynamic, context: dynamic) = js("require('serverless-http')")(
    websocketApp,
    json(
        "request" to { request: dynamic, e: dynamic ->
            request.connectionId = e.requestContext.connectionId
            request.domainName = e.requestContext.domainName
            request.event = e
        },
    ),
)(event, context)

private suspend fun handleConnect(request: Request, connectionId: String, event: Any?): Int {
    val commandDispatcher = with(request) { commandDispatcher(user, scope, traceId) }
    val partyId = request.query["partyId"].toString().let(::PartyId)
    val result = commandDispatcher.perform(ConnectPartyUserCommand(partyId, connectionId))
    return if (result == null) {
        403
    } else {
        with(result) { first.filterNot { it.connectionId == connectionId } to second }
            .broadcast(commandDispatcher)
        notifyConnectLambda(event).await()
        200
    }
}

private fun notifyConnectLambda(event: dynamic): Promise<Unit> {
    val options = notifyLambdaOptions()
    val client = LambdaClient(options)
    val commandOptions = json(
        "FunctionName" to "coupling-server-${Process.getEnv("STAGE")}-notifyConnect",
        "InvocationType" to "Event",
        "Payload" to JSON.stringify(event),
    )
    return client.send<Unit>(InvokeCommand(commandOptions)).catch {
        console.log("lambda invoke fail", it)
    }
}

private fun notifyLambdaOptions() = if (Process.getEnv("IS_OFFLINE") == "true") {
    json(
        "endpoint" to Process.getEnv("LAMBDA_ENDPOINT"),
        "region" to "us-east-1",
        "credentials" to json(
            "accessKeyId" to "lol",
            "secretAccessKey" to "lol",
        ),
    )
} else {
    json()
}

@JsExport
@JsName("serverlessSocketMessage")
fun serverlessSocketMessage(event: Json): dynamic {
    val connectionId = event.at<String>("/requestContext/connectionId") ?: ""
    println("message $connectionId")
    val message = event.at<String>("body")?.fromJsonString<JsonMessage>()?.toModel()
    return MainScope().promise {
        val socketDispatcher = socketDispatcher()
        if (message is PairAssignmentAdjustmentMessage) {
            socketDispatcher.perform(
                ReportDocCommand(connectionId, message.currentPairAssignments),
            )?.broadcast(socketDispatcher)
        }
        json("statusCode" to 200)
    }
}

@JsExport
@JsName("notifyConnect")
fun notifyConnect(event: Json) = MainScope().promise {
    val connectionId = event.at<String>("/requestContext/connectionId") ?: ""
    println("notifyConnect $connectionId")
    val socketDispatcher = socketDispatcher()
    socketDispatcher.perform(ConnectionsQuery(connectionId))
        ?.let { results ->
            socketDispatcher.managementApiClient.send(
                PostToConnectionCommand(
                    json("ConnectionId" to connectionId, "Data" to results.second.toSerializable().toJsonString()),
                ),
            )
        }
}.then {
    json("statusCode" to 200)
}.catch {
    console.log("Notify error", it)
    json("statusCode" to 500)
}

@JsExport
@JsName("serverlessSocketDisconnect")
fun serverlessSocketDisconnect(event: dynamic) = MainScope().promise {
    val connectionId = "${event.requestContext.connectionId}"
    println("disconnect $connectionId")
    val socketDispatcher = socketDispatcher()

    socketDispatcher
        .perform(DisconnectPartyUserCommand(connectionId))
        ?.broadcast(socketDispatcher)
        .let { json("statusCode" to 200) }
}

private suspend fun CoroutineScope.socketDispatcher() = commandDispatcher(
    UserDetails(
        UserId("websocket".toNotBlankString().getOrThrow()),
        "websocket".toNotBlankString().getOrThrow(),
        emptySet(),
        null,
        null,
    ),
    this,
    Uuid.random(),
)

private suspend fun Pair<List<CouplingConnection>, CouplingSocketMessage>.broadcast(socketDispatcher: CommandDispatcher) = socketDispatcher.perform(BroadcastAction(first, second))

private fun delete(connectionId: String, managementApi: ApiGatewayManagementApiClient) = managementApi.send(
    DeleteConnectionCommand(json("ConnectionId" to connectionId)),
)
