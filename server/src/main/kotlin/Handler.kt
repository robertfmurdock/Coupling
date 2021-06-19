@file:Suppress("HttpUrlsUsage")

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.dynamo.external.awsgatewaymanagement.ApiGatewayManagementApi
import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.json.toMessage
import com.zegreatrob.coupling.model.CouplingConnection
import com.zegreatrob.coupling.model.CouplingSocketMessage
import com.zegreatrob.coupling.model.PairAssignmentAdjustmentMessage
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.server.CommandDispatcher
import com.zegreatrob.coupling.server.Process
import com.zegreatrob.coupling.server.action.connection.ConnectTribeUserCommand
import com.zegreatrob.coupling.server.action.connection.ConnectionsQuery
import com.zegreatrob.coupling.server.action.connection.DisconnectTribeUserCommand
import com.zegreatrob.coupling.server.action.connection.ReportDocCommand
import com.zegreatrob.coupling.server.buildApp
import com.zegreatrob.coupling.server.commandDispatcher
import com.zegreatrob.coupling.server.express.Config
import com.zegreatrob.coupling.server.express.middleware.middleware
import com.zegreatrob.coupling.server.external.awssdk.clientlambda.InvokeCommand
import com.zegreatrob.coupling.server.external.awssdk.clientlambda.LambdaClient
import com.zegreatrob.coupling.server.external.express.express
import com.zegreatrob.minjson.at
import kotlinx.coroutines.*
import kotlin.js.Json
import kotlin.js.Promise
import kotlin.js.json

@Suppress("unused")
@JsExport
@JsName("serverless")
fun serverless(event: dynamic, context: dynamic): dynamic {
    event.path = if (event.path.unsafeCast<String?>() == "") "/" else event.path
    return js("require('serverless-http')")(app)(event, context)
}

private val app by lazy {
    buildApp()
}

@Suppress("unused")
@JsExport
@JsName("serverlessSocketConnect")
fun serverlessSocketConnect(event: dynamic, context: dynamic): dynamic {
    val connectionId = "${event.requestContext.connectionId}"
    println("connect $connectionId")
    val managementApi = apiGatewayManagementApi(event)

    val app = express()
    app.middleware()
    app.all("*") { request, response, _ ->
        if (!request.isAuthenticated()) {
            println("SOCKET NOT AUTH'D")
            delete(connectionId, managementApi)
                .promise().then {
                    response.sendStatus(403)
                }.catch {
                    console.log("problem disconnecting during connect", it)
                }
        } else {
            request.scope.launch {
                val commandDispatcher = with(request) { commandDispatcher(this.user, this.scope, this.traceId) }
                val tribeId = request.query["tribeId"].toString().let(::TribeId)
                val result = commandDispatcher.execute(ConnectTribeUserCommand(tribeId, connectionId))
                if (result == null) {
                    delete(connectionId, managementApi).promise().await()
                    response.sendStatus(403)
                } else {
                    with(result) { first.filterNot { it.connectionId == connectionId } to second }
                        .broadcast(managementApi, commandDispatcher)
                    notifyConnectLambda(event).await()
                    response.sendStatus(200)
                }
            }.invokeOnCompletion { cause: Throwable? ->
                cause?.let {
                    println("error $cause")

                    delete(connectionId, managementApi).promise().then {
                        response.sendStatus(403)
                    }.catch {
                        console.log("problem disconnecting during connect", it)
                    }
                }
            }
        }
    }

    return js("require('serverless-http')")(app)(event, context)
}

private fun notifyConnectLambda(event: dynamic): Promise<Unit> {
    val options = notifyLambdaOptions()
    val client = LambdaClient(options)
    return client.send<Unit>(
        InvokeCommand(
            json(
                "FunctionName" to "coupling-server-${Process.getEnv("STAGE")}-notifyConnect",
                "InvocationType" to "Event",
                "Payload" to JSON.stringify(event)
            )
        )
    ).catch {
        console.log("lambda invoke fail", it)
    }
}

private fun notifyLambdaOptions() = if (Process.getEnv("IS_OFFLINE") == "true")
    json(
        "endpoint" to Process.getEnv("LAMBDA_ENDPOINT"),
        "region" to "us-east-1",
        "credentials" to json(
            "accessKeyId" to "lol",
            "secretAccessKey" to "lol"
        )
    )
else
    json()


@Suppress("unused")
@JsExport
@JsName("serverlessSocketMessage")
fun serverlessSocketMessage(event: Json, context: dynamic): dynamic {
    val connectionId = event.at<String>("/requestContext/connectionId") ?: ""
    println("message $connectionId")
    val managementApi = apiGatewayManagementApi(event)

    val message = event.at<String>("body")?.let { JSON.parse<Json>(it) }?.toMessage()
    return MainScope().promise {
        val socketDispatcher = socketDispatcher()
        when (message) {
            is PairAssignmentAdjustmentMessage -> {
                socketDispatcher.execute(
                    ReportDocCommand(connectionId, message.currentPairAssignments)
                )?.broadcast(managementApi, socketDispatcher)
            }
            else -> {
            }
        }
        json("statusCode" to 200)
    }
}

@Suppress("unused")
@JsExport
@JsName("notifyConnect")
fun notifyConnect(event: Json, context: dynamic): dynamic {
    val connectionId = event.at<String>("/requestContext/connectionId") ?: ""
    println("notifyConnect $connectionId")
    val managementApi = apiGatewayManagementApi(event)
    return MainScope().promise {
        val socketDispatcher = socketDispatcher()
        socketDispatcher.execute(
            ConnectionsQuery(connectionId)
        )?.let { results ->
            managementApi.postToConnection(
                json("ConnectionId" to connectionId, "Data" to JSON.stringify(results.second.toJson()))
                    .also { console.log("Sending message to ", connectionId, JSON.stringify(it)) }
            ).promise()
        }
    }.then {
        json("statusCode" to 200)
    }.catch {
        console.log("Notify error", it)
        json("statusCode" to 500)
    }
}

@Suppress("unused")
@JsExport
@JsName("serverlessSocketDisconnect")
fun serverlessSocketDisconnect(event: dynamic, context: dynamic): dynamic {
    val connectionId = "${event.requestContext.connectionId}"
    println("disconnect $connectionId")

    val managementApi = apiGatewayManagementApi(event)

    return MainScope().promise {
        val socketDispatcher = socketDispatcher()
        socketDispatcher
            .execute(DisconnectTribeUserCommand(connectionId))
            ?.broadcast(managementApi, socketDispatcher)
        json("statusCode" to 200)
    }
}

private fun apiGatewayManagementApi(event: dynamic): ApiGatewayManagementApi {
    val domainName = "${event.requestContext.domainName}"
        .let { if (it.contains("localhost")) "http://${Config.websocketHost}" else it }
    return ApiGatewayManagementApi(
        json(
            "apiVersion" to "2018-11-29",
            "endpoint" to domainName
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
}

private suspend fun CoroutineScope.socketDispatcher() = commandDispatcher(
    User("websocket", "websocket", emptySet()), this, uuid4()
)

private suspend fun Pair<List<CouplingConnection>, CouplingSocketMessage>.broadcast(
    managementApi: ApiGatewayManagementApi,
    socketDispatcher: CommandDispatcher
) {
    println("Broadcasting to ${first.size} connections")

    val deadConnections = Promise.all(this.first.map { connection ->
        managementApi.postToConnection(
            json("ConnectionId" to connection.connectionId, "Data" to JSON.stringify(second.toJson()))
                .also { console.log("Sending message to ", connection.connectionId, JSON.stringify(it)) }
        ).promise()
            .then({ null }, { oops -> println("oops $oops"); connection.connectionId })
    }.toTypedArray())
        .await()

    deadConnections.filterNotNull().forEach {
        socketDispatcher.execute(DisconnectTribeUserCommand(it))
    }
}

private fun delete(connectionId: String, managementApi: ApiGatewayManagementApi) = managementApi.deleteConnection(
    json("ConnectionId" to connectionId)
)