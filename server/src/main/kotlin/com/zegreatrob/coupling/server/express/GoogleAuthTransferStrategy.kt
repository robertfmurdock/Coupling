package com.zegreatrob.coupling.server.express

import com.zegreatrob.coupling.server.UserDataService
import com.zegreatrob.coupling.server.external.express.Request
import com.zegreatrob.coupling.server.external.googleauthlibrary.OAuth2Client
import com.zegreatrob.coupling.server.external.passportcustom.Strategy
import kotlinx.coroutines.await
import kotlin.js.json

fun googleAuthTransferStrategy(): dynamic {
    val clientID = Config.googleClientID
    val client = OAuth2Client(clientID)

    return Strategy { request, done ->
        request.scope.async(done) {
            val payload = client.verifyIdToken(request, clientID).getPayload()
            UserDataService.findOrCreate(payload.email, request.traceId, request.scope)
        }
    }
}

private suspend fun OAuth2Client.verifyIdToken(request: Request, clientID: String) = verifyIdToken(
    json("idToken" to request.body.idToken, "audience" to clientID)
).await()