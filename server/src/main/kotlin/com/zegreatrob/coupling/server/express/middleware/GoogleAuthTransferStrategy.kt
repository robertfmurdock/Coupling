package com.zegreatrob.coupling.server.express.middleware

import com.zegreatrob.coupling.server.UserDataService.findOrCreateUser
import com.zegreatrob.coupling.server.express.Config
import com.zegreatrob.coupling.server.express.async
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
            findOrCreateUser(payload.email, request.traceId)
        }
    }
}

private suspend fun OAuth2Client.verifyIdToken(request: Request, clientID: String) = verifyIdToken(
    json("idToken" to request.body.idToken, "audience" to clientID)
).await()