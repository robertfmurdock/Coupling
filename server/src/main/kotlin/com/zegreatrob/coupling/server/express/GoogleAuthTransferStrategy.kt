package com.zegreatrob.coupling.server.express

import com.zegreatrob.coupling.server.UserDataService
import com.zegreatrob.coupling.server.external.googleauthlibrary.OAuth2Client
import com.zegreatrob.coupling.server.external.passportcustom.Strategy
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.await
import kotlinx.coroutines.promise
import kotlin.js.json

fun googleAuthTransferStrategy(): dynamic {
    val clientID = Config.googleClientID
    val client = OAuth2Client(clientID)

    return Strategy { request, done ->
        MainScope().promise {
            val payload = client.verifyIdToken(
                json("idToken" to request.body.idToken, "audience" to clientID)
            ).await().getPayload()

            UserDataService.findOrCreate(
                payload.email,
                request.traceId
            )
        }.then({ done(null, it) }, { done(it, null) })
    }
}