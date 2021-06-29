
import com.zegreatrob.coupling.dynamo.external.awsgatewaymanagement.ApiGatewayManagementApi
import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.model.Message
import com.zegreatrob.coupling.server.action.SocketCommunicator
import kotlinx.coroutines.await
import kotlin.js.json

interface AwsManagementApiSyntax {
    val managementApi: ApiGatewayManagementApi
}

interface AwsSocketCommunicator : SocketCommunicator, AwsManagementApiSyntax {
    override suspend fun sendMessageAndReturnIdWhenFail(connectionId: String, message: Message): String? =
        managementApi.postToConnection(
            json("ConnectionId" to connectionId, "Data" to JSON.stringify(message.toJson()))
                .also { console.log("Sending message to ", connectionId, JSON.stringify(it)) }
        ).promise()
            .then({ null }, { oops -> println("oops $oops"); connectionId })
            .await()
}
