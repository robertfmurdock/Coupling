import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.model.CouplingConnection
import com.zegreatrob.coupling.model.CouplingSocketMessage
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.server.action.connection.ConnectTribeUserCommand
import com.zegreatrob.coupling.server.buildApp
import com.zegreatrob.coupling.server.commandDispatcher
import com.zegreatrob.coupling.server.express.middleware.middleware
import com.zegreatrob.coupling.server.external.express.express
import com.zegreatrob.coupling.server.external.node_fetch.fetch
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
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

    val app = express()
    app.middleware()
    app.all("*") { request, _, _ ->
        if (!request.isAuthenticated()) {
            delete(connectionId)
        } else {
            request.scope.launch(block = {
                val commandDispatcher = with(request) { commandDispatcher(this.user, this.scope, this.traceId) }
                val tribeId = request.query["tribeId"].toString().let(::TribeId)

                val result = commandDispatcher.execute(ConnectTribeUserCommand(tribeId, connectionId))

                Promise.all(result?.first?.map { connection ->
                    connection.sendMessage(result.second)
                }?.toTypedArray() ?: emptyArray()).await()
            }).invokeOnCompletion { cause: Throwable? ->
                cause?.let {
                    println("error $cause")
                    delete(connectionId)
                }
            }
        }
    }

    return js("require('serverless-http')")(app)(event, context)
}

private fun CouplingConnection.sendMessage(couplingSocketMessage: CouplingSocketMessage) = fetch(
    "http://localhost:3001/@connections/$connectionId", json(
        "method" to "POST",
        "headers" to json("Content-Type" to "application/json"),
        "body" to JSON.stringify(couplingSocketMessage.toJson()),
    )
)

private fun delete(connectionId: String): Promise<Any> = fetch(
    "http://localhost:3001/@connections/${connectionId}", json("method" to "DELETE")
)