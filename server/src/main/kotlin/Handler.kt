@file:Suppress("HttpUrlsUsage")

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.dynamo.external.ApiGatewayManagementApi
import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.json.toPairAssignmentDocument
import com.zegreatrob.coupling.model.CouplingConnection
import com.zegreatrob.coupling.model.CouplingSocketMessage
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.server.action.connection.ConnectTribeUserCommand
import com.zegreatrob.coupling.server.action.connection.DisconnectTribeUserCommand
import com.zegreatrob.coupling.server.action.connection.ReportDocCommand
import com.zegreatrob.coupling.server.buildApp
import com.zegreatrob.coupling.server.commandDispatcher
import com.zegreatrob.coupling.server.express.Config
import com.zegreatrob.coupling.server.express.middleware.middleware
import com.zegreatrob.coupling.server.external.express.express
import com.zegreatrob.minjson.at
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
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
    console.log("event headers", event.headers)
    val managementApi = apiGatewayManagementApi(event)

    val app = express()
    app.middleware()
    app.all("*") { request, response, _ ->
        println("EXPRESS REQUEST'D")

        if (!request.isAuthenticated()) {
            println("SOCKET NOT AUTH'D")
            delete(connectionId, managementApi)
        } else {
            request.scope.launch(block = {
                val commandDispatcher = with(request) { commandDispatcher(this.user, this.scope, this.traceId) }
                val tribeId = request.query["tribeId"].toString().let(::TribeId)

                val result = commandDispatcher.execute(ConnectTribeUserCommand(tribeId, connectionId))
                result.broadcast(managementApi)
                response.sendStatus(200)
            }).invokeOnCompletion { cause: Throwable? ->
                cause?.let {
                    println("error $cause")
                    delete(connectionId, managementApi)
                }
            }
        }
    }

    return js("require('serverless-http')")(app)(event, context)
}

@Suppress("unused")
@JsExport
@JsName("serverlessSocketMessage")
fun serverlessSocketMessage(event: Json, context: dynamic): dynamic {
    val connectionId = event.at<String>("/requestContext/connectionId") ?: ""
    println("message $connectionId")
    val managementApi = apiGatewayManagementApi(event)

    val pairAssignmentDocument = event.at<Json>("body/data/currentPairAssignments")
        ?.toPairAssignmentDocument()

    MainScope().launch {
        socketDispatcher().execute(
            ReportDocCommand(connectionId, pairAssignmentDocument)
        ).broadcast(managementApi)
    }

    return null
}

@Suppress("unused")
@JsExport
@JsName("serverlessSocketDisconnect")
fun serverlessSocketDisconnect(event: dynamic, context: dynamic): dynamic {
    val connectionId = "${event.requestContext.connectionId}"
    println("disconnect $connectionId")

    val managementApi = apiGatewayManagementApi(event)

    MainScope().launch {
        socketDispatcher()
            .execute(DisconnectTribeUserCommand(connectionId))
            .broadcast(managementApi)
    }
    return null
}

private fun apiGatewayManagementApi(event: dynamic): ApiGatewayManagementApi {
    val domainName = "${event.requestContext.domainName}"
        .let { if (it == "localhost") "http://${Config.websocketHost}" else it }
    val stage = "${event.requestContext.stage}".let { if (it == "local") "" else it }
    return ApiGatewayManagementApi(
        json(
            "apiVersion" to "2018-11-29",
            "endpoint" to "$domainName/$stage"
        )
    )
}

private suspend fun CoroutineScope.socketDispatcher() =
    commandDispatcher(User("websocket", "websocket", emptySet()), this, uuid4())

private suspend fun Pair<List<CouplingConnection>, CouplingSocketMessage>?.broadcast(managementApi: ApiGatewayManagementApi) {
    Promise.all(this?.first?.map { connection ->
        console.log("Sending message to ", connection.connectionId)
        connection.sendMessage(second, managementApi)
            .catch { oops -> println("oops $oops") }
    }?.toTypedArray() ?: emptyArray())
        .await()
}

private fun CouplingConnection.sendMessage(
    couplingSocketMessage: CouplingSocketMessage,
    managementApi: ApiGatewayManagementApi
): Promise<Json> {
    return managementApi.postToConnection(
        json("ConnectionId" to connectionId, "Data" to JSON.stringify(couplingSocketMessage.toJson()))
    ).promise()
}

private fun delete(connectionId: String, managementApi: ApiGatewayManagementApi) = managementApi.deleteConnection(
    json("ConnectionId" to connectionId)
)