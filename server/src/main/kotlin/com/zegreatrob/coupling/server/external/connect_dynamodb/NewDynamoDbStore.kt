package com.zegreatrob.coupling.server.external.connect_dynamodb

import com.zegreatrob.coupling.server.external.express_session.expressSession
import kotlin.js.Json

fun newDynamoDbStore(@Suppress("UNUSED_PARAMETER") config: Json): dynamic {
    @Suppress("UNUSED_VARIABLE") val store =
        connectDynamoDb(expressSession)
    return js("new store(config)")
}