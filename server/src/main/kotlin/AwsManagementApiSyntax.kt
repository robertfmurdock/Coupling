import com.zegreatrob.coupling.json.toJsonString
import com.zegreatrob.coupling.json.toSerializable
import com.zegreatrob.coupling.model.Message
import com.zegreatrob.coupling.repository.dynamo.external.awsgatewaymanagement.ApiGatewayManagementApiClient
import com.zegreatrob.coupling.repository.dynamo.external.awsgatewaymanagement.PostToConnectionCommand
import com.zegreatrob.coupling.server.action.SocketCommunicator
import kotlinx.coroutines.await
import kotlin.js.json

interface AwsManagementApiSyntax {
//    val managementApi: ApiGatewayManagementApi

    val managementApiClient: ApiGatewayManagementApiClient
}

interface AwsSocketCommunicator : SocketCommunicator, AwsManagementApiSyntax {
    override suspend fun sendMessageAndReturnIdWhenFail(connectionId: String, message: Message): String? =
//        managementApi.postToConnection(
//            json("ConnectionId" to connectionId, "Data" to message.toSerializable().toJsonString())
//        ).promise()
//            .then({ null }, { oops -> println("oops $oops"); connectionId })
//            .await()
//
        managementApiClient.send(
            PostToConnectionCommand(
                json("ConnectionId" to connectionId, "Data" to message.toSerializable().toJsonString())
            )
        )
            .then({ null }, { oops -> println("oops $oops"); connectionId })
            .await()
}
