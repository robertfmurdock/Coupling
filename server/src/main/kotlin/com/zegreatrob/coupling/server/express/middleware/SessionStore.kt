package com.zegreatrob.coupling.server.express.middleware

import com.zegreatrob.coupling.dynamo.DynamoDbProvider
import com.zegreatrob.coupling.server.external.connect_dynamodb.newDynamoDbStore
import kotlin.js.json

fun sessionStore() = newDynamoDbStore(
    json("client" to DynamoDbProvider.dynamoDB)
)
